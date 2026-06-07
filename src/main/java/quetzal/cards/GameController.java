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
            return ActionResult.failure("Action cannot be null.");
        }

        if (action instanceof DrawFromDeckAction drawAction) {
            return drawFromDeck(drawAction);
        }

        if (action instanceof DiscardAction discardAction) {
            return discard(discardAction);
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

        return ActionResult.failure("Unsupported action: " + action.getClass().getSimpleName());
    }

    private ActionResult drawFromDeck(DrawFromDeckAction action) {
        PlayerId playerId = action.playerId();
        RoundState roundState = gameState.roundState();

        if (!playerId.equals(roundState.activePlayerId())) {
            return ActionResult.failure("Only the active player may draw.");
        }

        if (roundState.turnPhase() != TurnPhase.DRAW_OR_CASTIGO) {
            return ActionResult.failure("Cannot draw during phase: " + roundState.turnPhase());
        }

        if (gameState.deck().getCards().isEmpty()) {
            gameState.deck().addShuffledStandardDeckWithJokers();
        }

        Card drawnCard = gameState.deck().drawCard();
        gameState.player(playerId).addCard(drawnCard);

        TurnPhase previousPhase = roundState.turnPhase();
        roundState.setTurnPhase(TurnPhase.MELD);

        return ActionResult.success(
                new CardDrawnEvent(playerId, drawnCard),
                new TurnPhaseChangedEvent(previousPhase, roundState.turnPhase())
        );
    }


    private ActionResult discard(DiscardAction action) {
        PlayerId playerId = action.playerId();
        RoundState roundState = gameState.roundState();

        if (!playerId.equals(roundState.activePlayerId())) {
            return ActionResult.failure("Only the active player may discard.");
        }

        if (roundState.turnPhase() != TurnPhase.MELD) {
            return ActionResult.failure("Cannot discard during phase: " + roundState.turnPhase());
        }

        Card discardedCard = gameState.player(playerId).removeCard(action.cardId());
        gameState.discardPile().add(discardedCard);

        TurnPhase previousPhase = roundState.turnPhase();
        PlayerId previousActivePlayer = roundState.activePlayerId();
        PlayerId nextActivePlayer = gameState.nextPlayerAfter(previousActivePlayer);

        roundState.setActivePlayerId(nextActivePlayer);
        roundState.setTurnPhase(TurnPhase.DRAW_OR_CASTIGO);

        return ActionResult.success(
                new CardDiscardedEvent(playerId, discardedCard),
                new ActivePlayerChangedEvent(previousActivePlayer, nextActivePlayer),
                new TurnPhaseChangedEvent(previousPhase, roundState.turnPhase())
        );
    }


    private ActionResult createMeld(CreateMeldAction action) {
        PlayerId playerId = action.playerId();
        RoundState roundState = gameState.roundState();

        if (!playerId.equals(roundState.activePlayerId())) {
            return ActionResult.failure("Only the active player may create a meld.");
        }

        if (roundState.turnPhase() != TurnPhase.MELD) {
            return ActionResult.failure("Cannot create a meld during phase: " + roundState.turnPhase());
        }

        PlayerState player = gameState.player(playerId);
        List<Card> selectedCards = player.cardsByIdInOrder(action.cardIds());
        MeldValidationResult validationResult = meldValidator.validate(selectedCards);

        if (!validationResult.valid()) {
            return ActionResult.failure(validationResult.displayText());
        }

        List<Card> cardsForMeld = validationResult.normalizedCards();

        for (Card card : cardsForMeld) {
            player.removeCard(card.id());
        }

        MeldState meld = new MeldState(playerId, validationResult.meldType(), cardsForMeld);
        gameState.playArea().addMeld(meld);

        return ActionResult.success(new MeldCreatedEvent(playerId, meld));
    }


    private ActionResult sortHandByRank(SortHandByRankAction action) {
        PlayerId playerId = action.playerId();

        if (!playerId.equals(gameState.roundState().activePlayerId())) {
            return ActionResult.failure("Only the active player may sort their hand.");
        }

        PlayerState player = gameState.player(playerId);
        player.sortHandByRank();
        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
    }

    private ActionResult sortHandBySuit(SortHandBySuitAction action) {
        PlayerId playerId = action.playerId();

        if (!playerId.equals(gameState.roundState().activePlayerId())) {
            return ActionResult.failure("Only the active player may sort their hand.");
        }

        PlayerState player = gameState.player(playerId);
        player.sortHandBySuit();
        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
    }

    private ActionResult reorderHand(ReorderHandAction action) {
        PlayerId playerId = action.playerId();

        if (!playerId.equals(gameState.roundState().activePlayerId())) {
            return ActionResult.failure("Only the active player may reorder their hand.");
        }

        PlayerState player = gameState.player(playerId);
        player.reorderHand(action.orderedCardIds());
        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
    }

    private ActionResult saveCustomHandOrder(SaveCustomHandOrderAction action) {
        PlayerId playerId = action.playerId();

        if (!playerId.equals(gameState.roundState().activePlayerId())) {
            return ActionResult.failure("Only the active player may save their custom hand order.");
        }

        PlayerState player = gameState.player(playerId);
        player.saveCurrentHandOrderAsCustom();
        return ActionResult.success(new CustomHandOrderSavedEvent(playerId));
    }

    private ActionResult restoreCustomHandOrder(RestoreCustomHandOrderAction action) {
        PlayerId playerId = action.playerId();

        if (!playerId.equals(gameState.roundState().activePlayerId())) {
            return ActionResult.failure("Only the active player may restore their custom hand order.");
        }

        PlayerState player = gameState.player(playerId);
        player.restoreCustomHandOrder();
        return ActionResult.success(new HandOrderChangedEvent(playerId, player.hand()));
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
