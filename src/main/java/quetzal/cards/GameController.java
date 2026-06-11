package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlled mutation boundary for GameState.
 *
 * Milestone 5A keeps this intentionally small. Later, UI code should request
 * actions through this controller instead of directly mutating state.
 */
public final class GameController {

    private static final int DEFAULT_PLAYER_COUNT = 4;
    private static final int DEFAULT_HAND_SIZE = 13;
    private static final int DEFAULT_CASTIGOS_PER_GAME = 10;
    private static final int ACTIVE_CASTIGO_DECK_CARDS = 4;
    private static final int OUT_OF_TURN_CASTIGO_DECK_CARDS = 3;

    private final GameState gameState;
    private final MeldValidator meldValidator = new LaKikaMeldValidator();

    private GameController(GameState gameState) {
        this.gameState = gameState;
    }

    public static GameController newPrototypeGame() {
        Deck deck = Deck.laKikaPrototypeDeck();
        deck.shuffle();

        List<PlayerState> players = new ArrayList<>();

        for (int i = 1; i <= DEFAULT_PLAYER_COUNT; i++) {
            PlayerId playerId = new PlayerId(i);
            players.add(new PlayerState(
                    playerId,
                    "Player " + i,
                    0,
                    DEFAULT_CASTIGOS_PER_GAME,
                    false
            ));
        }

        PlayerId dealerId = new PlayerId(1);
        PlayerId activePlayerId = new PlayerId(2);

        RoundState roundState = new RoundState(
                1,
                dealerId,
                activePlayerId,
                TurnPhase.DRAW_OR_CASTIGO
        );

        GameState state = new GameState(players, deck, new DiscardPile(), new PlayArea(), roundState);
        GameController controller = new GameController(state);
        controller.dealInitialHands(DEFAULT_HAND_SIZE);

        return controller;
    }


    public static GameController fromSnapshot(GameStateSnapshot snapshot) {
        return new GameController(GameState.fromSnapshot(snapshot));
    }

    public ActionResult apply(GameAction action) {
        if (action == null) {
            return ActionResult.failure(ActionFailureCode.NULL_ACTION, "Action cannot be null.");
        }

        if (action instanceof DrawFromDeckAction drawAction) {
            return drawFromDeck(drawAction);
        }

        if (action instanceof DiscardAction discardAction) {
            return discard(discardAction);
        }

        if (action instanceof TakeCastigoAction takeCastigoAction) {
            return takeCastigo(takeCastigoAction);
        }

        if (action instanceof CreateMeldAction createMeldAction) {
            return createMeld(createMeldAction);
        }

        if (action instanceof SortHandByRankAction sortAction) {
            return sortHandByRank(sortAction);
        }

        if (action instanceof SortHandBySuitAction sortAction) {
            return sortHandBySuit(sortAction);
        }

        if (action instanceof ReorderHandAction reorderAction) {
            return reorderHand(reorderAction);
        }

        if (action instanceof SaveCustomHandOrderAction saveAction) {
            return saveCustomHandOrder(saveAction);
        }

        if (action instanceof RestoreCustomHandOrderAction restoreAction) {
            return restoreCustomHandOrder(restoreAction);
        }

        return ActionResult.failure(
                ActionFailureCode.UNSUPPORTED_ACTION,
                "Unsupported action: " + action.getClass().getSimpleName()
        );
    }

    private ActionResult drawFromDeck(DrawFromDeckAction action) {
        PlayerId playerId = action.playerId();

        ActionResult turnCheck = TurnRules.requireActivePlayerInPhase(
                gameState,
                playerId,
                TurnPhase.DRAW_OR_CASTIGO,
                "draw from the deck"
        );

        if (turnCheck != null) {
            return turnCheck;
        }

        if (gameState.deck().getCards().isEmpty()) {
            gameState.deck().addShuffledStandardDeckWithJokers();
        }

        Card drawnCard = gameState.deck().drawCard();
        gameState.player(playerId).addCard(drawnCard);

        TurnPhaseChangedEvent phaseChangedEvent = advanceAfterDraw();

        return ActionResult.success(
                new CardDrawnEvent(playerId, drawnCard),
                phaseChangedEvent
        );
    }


    private ActionResult discard(DiscardAction action) {
        PlayerId playerId = action.playerId();

        ActionResult turnCheck = TurnRules.requireActivePlayerInPhase(
                gameState,
                playerId,
                TurnPhase.MELD,
                "discard"
        );

        if (turnCheck != null) {
            return turnCheck;
        }

        Card discardedCard;

        try {
            discardedCard = gameState.player(playerId).removeCard(action.cardId());
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionFailureCode.CARD_NOT_IN_HAND, exception.getMessage());
        }

        gameState.discardPile().add(discardedCard);

        TurnPhase previousPhase = gameState.roundState().turnPhase();
        PlayerId previousActivePlayer = gameState.roundState().activePlayerId();
        PlayerId nextActivePlayer = gameState.nextPlayerAfter(previousActivePlayer);

        advanceAfterDiscard(nextActivePlayer);

        return ActionResult.success(
                new CardDiscardedEvent(playerId, discardedCard),
                new ActivePlayerChangedEvent(previousActivePlayer, nextActivePlayer),
                new TurnPhaseChangedEvent(previousPhase, gameState.roundState().turnPhase())
        );
    }


    private ActionResult takeCastigo(TakeCastigoAction action) {
        PlayerId playerId = action.playerId();

        ActionResult turnCheck = TurnRules.requireActivePlayerInPhase(
                gameState,
                playerId,
                TurnPhase.DRAW_OR_CASTIGO,
                "take castigo"
        );

        if (turnCheck != null) {
            return turnCheck;
        }

        if (gameState.discardPile().isEmpty()) {
            return ActionResult.failure(
                    ActionFailureCode.NO_CASTIGO_AVAILABLE,
                    "No castigo is available because the discard pile is empty."
            );
        }

        PlayerState player = gameState.player(playerId);

        if (player.castigosRemaining() <= 0) {
            return ActionResult.failure(
                    ActionFailureCode.NO_CASTIGOS_REMAINING,
                    "Player has no castigos remaining."
            );
        }

        player.consumeCastigo();

        Card discardCard = gameState.discardPile().removeTopCard();
        player.addCard(discardCard);

        List<Card> drawnCards = drawCastigoDeckCards(ACTIVE_CASTIGO_DECK_CARDS);

        for (Card drawnCard : drawnCards) {
            player.addCard(drawnCard);
        }

        TurnPhaseChangedEvent phaseChangedEvent = advanceAfterDraw();

        return ActionResult.success(
                new CastigoTakenEvent(playerId, discardCard, drawnCards),
                phaseChangedEvent
        );
    }

    private List<Card> drawCastigoDeckCards(int deckCardCount) {
        List<Card> drawnCards = new ArrayList<>();

        for (int i = 0; i < deckCardCount; i++) {
            if (gameState.deck().getCards().isEmpty()) {
                gameState.deck().addShuffledStandardDeckWithJokers();
            }

            drawnCards.add(gameState.deck().drawCard());
        }

        return drawnCards;
    }


    private ActionResult createMeld(CreateMeldAction action) {
        PlayerId playerId = action.playerId();

        ActionResult turnCheck = TurnRules.requireActivePlayerInPhase(
                gameState,
                playerId,
                TurnPhase.MELD,
                "create a meld"
        );

        if (turnCheck != null) {
            return turnCheck;
        }

        PlayerState player = gameState.player(playerId);
        List<Card> selectedCards;

        try {
            selectedCards = player.cardsByIdInOrder(action.cardIds());
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionFailureCode.CARD_NOT_IN_HAND, exception.getMessage());
        }

        MeldValidationResult validationResult = meldValidator.validate(selectedCards);

        if (!validationResult.valid()) {
            return ActionResult.failure(ActionFailureCode.INVALID_MELD, validationResult.displayText());
        }

        List<Card> cardsForMeld = validationResult.normalizedCards();
        MeldState meld = new MeldState(playerId, validationResult.meldType(), cardsForMeld);
        ActionResult openingCheck = validateOpeningPermission(player, meld);

        if (openingCheck != null) {
            return openingCheck;
        }

        for (Card card : cardsForMeld) {
            player.removeCard(card.id());
        }

        gameState.playArea().addMeld(meld);

        if (!player.opened() && openingRequirement().isSatisfiedBy(gameState.playArea().meldsCreatedBy(playerId))) {
            player.setOpened(true);
            return ActionResult.success(
                    new MeldCreatedEvent(playerId, meld),
                    new PlayerOpenedEvent(playerId)
            );
        }

        return ActionResult.success(new MeldCreatedEvent(playerId, meld));
    }


    private ActionResult validateOpeningPermission(PlayerState player, MeldState candidateMeld) {
        if (player.opened()) {
            return null;
        }

        OpeningRequirement requirement = openingRequirement();

        if (!requirement.acceptsAsOpeningMeld(candidateMeld)) {
            return ActionResult.failure(
                    ActionFailureCode.OPENING_REQUIREMENT_NOT_MET,
                    "Closed player must satisfy opening requirement before playing freely. " + requirement.displayText()
            );
        }

        return null;
    }

    private OpeningRequirement openingRequirement() {
        return OpeningRequirement.forRound(gameState.roundState().roundNumber());
    }

    private ActionResult sortHandByRank(SortHandByRankAction action) {
        PlayerId playerId = action.playerId();

        ActionResult activePlayerCheck = TurnRules.requireActivePlayer(gameState, playerId, "sort their hand");

        if (activePlayerCheck != null) {
            return activePlayerCheck;
        }

        PlayerState player = gameState.player(playerId);
        player.sortHandByRank();
        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
    }

    private ActionResult sortHandBySuit(SortHandBySuitAction action) {
        PlayerId playerId = action.playerId();

        ActionResult activePlayerCheck = TurnRules.requireActivePlayer(gameState, playerId, "sort their hand");

        if (activePlayerCheck != null) {
            return activePlayerCheck;
        }

        PlayerState player = gameState.player(playerId);
        player.sortHandBySuit();
        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
    }

    private ActionResult reorderHand(ReorderHandAction action) {
        PlayerId playerId = action.playerId();

        ActionResult activePlayerCheck = TurnRules.requireActivePlayer(gameState, playerId, "reorder their hand");

        if (activePlayerCheck != null) {
            return activePlayerCheck;
        }

        PlayerState player = gameState.player(playerId);

        try {
            player.reorderHand(action.orderedCardIds());
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionFailureCode.INVALID_HAND_ORDER, exception.getMessage());
        }

        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
    }

    private ActionResult saveCustomHandOrder(SaveCustomHandOrderAction action) {
        PlayerId playerId = action.playerId();

        ActionResult activePlayerCheck = TurnRules.requireActivePlayer(gameState, playerId, "save their custom hand order");

        if (activePlayerCheck != null) {
            return activePlayerCheck;
        }

        PlayerState player = gameState.player(playerId);
        player.saveCurrentHandOrderAsCustom();
        return ActionResult.success(new CustomHandOrderSavedEvent(playerId));
    }

    private ActionResult restoreCustomHandOrder(RestoreCustomHandOrderAction action) {
        PlayerId playerId = action.playerId();

        ActionResult activePlayerCheck = TurnRules.requireActivePlayer(gameState, playerId, "restore their custom hand order");

        if (activePlayerCheck != null) {
            return activePlayerCheck;
        }

        PlayerState player = gameState.player(playerId);
        player.restoreCustomHandOrder();
        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
    }

    private TurnPhaseChangedEvent advanceAfterDraw() {
        RoundState roundState = gameState.roundState();
        TurnPhase previousPhase = roundState.turnPhase();
        roundState.setTurnPhase(TurnPhase.MELD);
        return new TurnPhaseChangedEvent(previousPhase, roundState.turnPhase());
    }

    private void advanceAfterDiscard(PlayerId nextActivePlayer) {
        RoundState roundState = gameState.roundState();
        roundState.setActivePlayerId(nextActivePlayer);
        roundState.setTurnPhase(TurnPhase.DRAW_OR_CASTIGO);
    }

    public GameState state() {
        return gameState;
    }

    public List<PlayerHudState> hudStates() {
        return gameState.toHudStates();
    }

    public List<Card> handFor(PlayerId playerId) {
        return gameState.player(playerId).hand();
    }

    public Deck deck() {
        return gameState.deck();
    }

    private void dealInitialHands(int handSize) {
        for (int cardIndex = 0; cardIndex < handSize; cardIndex++) {
            for (PlayerState player : gameState.players()) {
                if (gameState.deck().getCards().isEmpty()) {
                    gameState.deck().addShuffledStandardDeckWithJokers();
                }

                player.addCard(gameState.deck().drawCard());
            }
        }

        for (PlayerState player : gameState.players()) {
            player.saveCurrentHandOrderAsCustom();
        }
    }
}
