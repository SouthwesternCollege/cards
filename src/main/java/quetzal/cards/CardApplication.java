package quetzal.cards;

import com.almasb.fxgl.app.CursorInfo;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CardApplication extends GameApplication {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    private Hand hand;
    private GameLayout gameLayout;
    private GameHUD gameHUD;
    private GameHudController gameHudController;
    private GameControls gameControls;
    private Deck deck;
    private DeckDiscardPanel deckDiscardPanel;
    private FullPlayAreaView fullPlayAreaView;
    private DebugHandOverlay debugHandOverlay;
    private DebugDrawer debugDrawer;
    private PassDeviceOverlay passDeviceOverlay;
    private GameController gameController;
    private GameActionPresentationAdapter actionPresentationAdapter;
    private List<PlayerId> pendingCastigoOfferPlayers = List.of();
    private int pendingCastigoOfferIndex = 0;
    private boolean prototypeGameStarted = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Cards");
        gameSettings.setAppIcon("icon.png");
        gameSettings.setVersion("0.0β");
        gameSettings.setFullScreenAllowed(true);
        gameSettings.setFullScreenFromStart(true);
        gameSettings.setWidth(WIDTH);
        gameSettings.setHeight(HEIGHT);
        gameSettings.setDefaultCursor(new CursorInfo("cursor.png", 0, 0));
        gameSettings.setSceneFactory(new SceneFactory() {
            @NotNull
            @Override
            public FXGLMenu newGameMenu() {
                return new PauseMenu();
            }
        });
    }

    @Override
    protected void initGame() {
        gameLayout = new GameLayout(WIDTH, HEIGHT);

        FXGL.loopBGM("theme.mp3");

        CardViewMetrics.configureFromStandardDeckSheet(
                FXGL.image("deck.png"),
                AnimationSettings.DEFAULT_CARD_RENDER_SCALE
        );

        FXGL.getGameWorld().addEntityFactory(new GameFactory());
    }

    @Override
    protected void initUI() {
        TitleScreenController titleScreenController = new TitleScreenController(WIDTH, HEIGHT);
        titleScreenController.show(this::showMainMenu);
    }

    private void showMainMenu() {
        MainMenuView mainMenuView = new MainMenuView(WIDTH, HEIGHT, this::startPrototypeGame);
        mainMenuView.show();
    }

    private void startPrototypeGame() {
        if (prototypeGameStarted) {
            return;
        }

        prototypeGameStarted = true;

        FXGL.spawn("Background", new SpawnData(0, 0).put("width", WIDTH).put("height", HEIGHT));

        addLayoutDebugOverlay();

        gameController = GameController.newPrototypeGame();
        deck = gameController.deck();

        PlayerHudModel playerHudModel = PlayerHudModel.fromGameState(gameController.state());
        gameHUD = new GameHUD(gameLayout, playerHudModel);
        gameHudController = new GameHudController(playerHudModel, gameHUD);
        gameHudController.refresh();

        Rectangle2D playerHandArea = gameLayout.getPlayerHandArea();
        PlayerId localPlayerId = gameController.state().roundState().activePlayerId();

        SelectionFeedback selectionFeedback = new HudMeldSelectionFeedback(
                new LaKikaMeldValidator(),
                gameHudController
        );
        HandChangeListener handChangeListener = cardsRemaining ->
                gameHudController.setCardsRemaining(localPlayerId, cardsRemaining);

        hand = new Hand(
                playerHandArea,
                gameLayout.getOpponentPlayedArea(),
                gameLayout.getPlayerPlayedArea(),
                deck,
                selectionFeedback,
                handChangeListener,
                this::reorderActiveHand
        );

        hand.setPlayerIds(gameController.state().players().stream()
                .map(PlayerState::playerId)
                .toList());
        hand.setPlayAreaPerspective(localPlayerId, gameController.state().nextPlayerAfter(localPlayerId));
        hand.populateHand(gameController.handFor(localPlayerId));

        fullPlayAreaView = new FullPlayAreaView(WIDTH, HEIGHT, gameController);
        debugHandOverlay = new DebugHandOverlay(WIDTH, HEIGHT, gameController);
        passDeviceOverlay = new PassDeviceOverlay(WIDTH, HEIGHT);
        debugDrawer = new DebugDrawer(
                WIDTH,
                HEIGHT,
                fullPlayAreaView::toggle,
                debugHandOverlay::toggle,
                hand::debugAddKindMeld,
                hand::debugAddStraightFlushMeld,
                hand::debugAddManyMelds,
                hand::debugClearVisualMelds
        );

        gameControls = new GameControls(
                gameLayout,
                hand,
                debugDrawer::toggle,
                this::discardSelectedCard,
                this::playSelectedMeld,
                this::addSelectedCardToTargetMeld,
                this::replaceJokerInTargetMeld,
                this::sortActiveHandByRank,
                this::sortActiveHandBySuit,
                this::restoreCustomHandOrder,
                this::saveCustomHandOrder
        );

        deckDiscardPanel = new DeckDiscardPanel(gameLayout, deck, new DeckDiscardActions() {
            @Override
            public void drawFromDeck(Point2D sourcePosition) {
                PlayerId activePlayerId = gameController.state().roundState().activePlayerId();
                ActionResult result = gameController.apply(new DrawFromDeckAction(activePlayerId));
                actionPresentationAdapter.handleActionResult(result, sourcePosition);

                if (result.success()) {
                    startOutOfTurnCastigoOffersAfterActiveDraw(activePlayerId);
                }
            }

            @Override
            public void takeCastigo(Point2D sourcePosition) {
                ActionResult result = gameController.apply(new TakeCastigoAction(gameController.state().roundState().activePlayerId()));
                actionPresentationAdapter.handleActionResult(result, sourcePosition);
            }

            @Override
            public void passCastigo() {
                passCurrentOutOfTurnCastigoOffer();
            }
        });

        actionPresentationAdapter = new GameActionPresentationAdapter(
                gameController,
                hand,
                gameHudController,
                deckDiscardPanel,
                debugHandOverlay,
                passDeviceOverlay
        );
    }





    private void startOutOfTurnCastigoOffersAfterActiveDraw(PlayerId activePlayerId) {
        if (gameController.state().discardPile().isEmpty()) {
            return;
        }

        pendingCastigoOfferPlayers = eligibleOutOfTurnCastigoPlayers(activePlayerId);
        pendingCastigoOfferIndex = 0;

        if (pendingCastigoOfferPlayers.isEmpty()) {
            return;
        }

        deckDiscardPanel.hideDecisionControls();

        FXGL.runOnce(() -> {
            hand.clearVisibleHand();
            showNextOutOfTurnCastigoOffer(activePlayerId);
        }, Duration.seconds(AnimationSettings.PLAYED_CARD_MOVE_SECONDS + 0.05));
    }

    private List<PlayerId> eligibleOutOfTurnCastigoPlayers(PlayerId activePlayerId) {
        List<PlayerId> result = new ArrayList<>();
        PlayerId current = gameController.state().nextPlayerAfter(activePlayerId);
        PlayerId topDiscardedBy = gameController.state().discardPile().topDiscardedBy().orElse(null);

        while (!current.equals(activePlayerId)) {
            PlayerState player = gameController.state().player(current);

            if (player.castigosRemaining() > 0 && !current.equals(topDiscardedBy)) {
                result.add(current);
            }

            current = gameController.state().nextPlayerAfter(current);
        }

        return result;
    }

    private void showNextOutOfTurnCastigoOffer(PlayerId activePlayerId) {
        if (gameController.state().discardPile().isEmpty()) {
            returnToActivePlayerAfterCastigoOffers(activePlayerId);
            return;
        }

        if (pendingCastigoOfferIndex >= pendingCastigoOfferPlayers.size()) {
            returnToActivePlayerAfterCastigoOffers(activePlayerId);
            return;
        }

        PlayerId offerPlayerId = pendingCastigoOfferPlayers.get(pendingCastigoOfferIndex);
        Card discardCard = gameController.state().discardPile().topCard().orElse(null);

        if (discardCard == null) {
            returnToActivePlayerAfterCastigoOffers(activePlayerId);
            return;
        }

        passDeviceOverlay.show(
                offerPlayerId,
                () -> passDeviceOverlay.showCastigoDecision(
                        offerPlayerId,
                        discardCard,
                        () -> takeOutOfTurnCastigo(offerPlayerId, activePlayerId),
                        () -> passCurrentOutOfTurnCastigoOffer(activePlayerId)
                )
        );
    }

    private void takeOutOfTurnCastigo(PlayerId offerPlayerId, PlayerId activePlayerId) {
        ActionResult result = gameController.apply(new TakeCastigoAction(offerPlayerId));
        actionPresentationAdapter.handleActionResult(result, discardPilePosition());

        if (result.success()) {
            returnToActivePlayerAfterCastigoOffers(activePlayerId);
            return;
        }

        pendingCastigoOfferIndex++;
        showNextOutOfTurnCastigoOffer(activePlayerId);
    }

    private void passCurrentOutOfTurnCastigoOffer() {
        passCurrentOutOfTurnCastigoOffer(gameController.state().roundState().activePlayerId());
    }

    private void passCurrentOutOfTurnCastigoOffer(PlayerId activePlayerId) {
        pendingCastigoOfferIndex++;
        showNextOutOfTurnCastigoOffer(activePlayerId);
    }

    private void returnToActivePlayerAfterCastigoOffers(PlayerId activePlayerId) {
        pendingCastigoOfferPlayers = List.of();
        pendingCastigoOfferIndex = 0;

        passDeviceOverlay.show(
                activePlayerId,
                () -> {
                    actionPresentationAdapter.renderActivePlayerHand(activePlayerId);
                    deckDiscardPanel.hideDecisionControls();
                }
        );
    }


    private void sortActiveHandByRank() {
        ActionResult result = gameController.apply(new SortHandByRankAction(
                gameController.state().roundState().activePlayerId()
        ));
        actionPresentationAdapter.handleActionResult(result, null);
    }

    private void sortActiveHandBySuit() {
        ActionResult result = gameController.apply(new SortHandBySuitAction(
                gameController.state().roundState().activePlayerId()
        ));
        actionPresentationAdapter.handleActionResult(result, null);
    }

    private void reorderActiveHand(List<Card> orderedCards) {
        List<CardId> orderedCardIds = orderedCards.stream()
                .map(Card::id)
                .toList();

        ActionResult result = gameController.apply(new ReorderHandAction(
                gameController.state().roundState().activePlayerId(),
                orderedCardIds
        ));
        actionPresentationAdapter.handleActionResult(result, null);
    }

    private void restoreCustomHandOrder() {
        ActionResult result = gameController.apply(new RestoreCustomHandOrderAction(
                gameController.state().roundState().activePlayerId()
        ));
        actionPresentationAdapter.handleActionResult(result, null);
    }

    private void saveCustomHandOrder() {
        ActionResult result = gameController.apply(new SaveCustomHandOrderAction(
                gameController.state().roundState().activePlayerId()
        ));
        actionPresentationAdapter.handleActionResult(result, null);
    }

    private void playSelectedMeld() {
        List<Card> selectedCards = hand.getSelectedCards();

        if (selectedCards.isEmpty()) {
            System.out.println("Select cards to play.");
            return;
        }

        List<CardId> selectedCardIds = selectedCards.stream()
                .map(Card::id)
                .toList();

        ActionResult result = gameController.apply(new CreateMeldAction(
                gameController.state().roundState().activePlayerId(),
                selectedCardIds
        ));

        actionPresentationAdapter.handleActionResult(result, null);
    }

    private void addSelectedCardToTargetMeld() {
        List<Card> selectedCards = hand.getSelectedCards();

        if (selectedCards.size() != 1) {
            System.out.println("Select exactly one card to add to a meld.");
            return;
        }

        MeldId targetMeldId = hand.selectedTargetMeldId();

        if (targetMeldId == null) {
            System.out.println("Select a target meld first by clicking a played meld.");
            return;
        }

        ActionResult result = gameController.apply(new AddCardToMeldAction(
                gameController.state().roundState().activePlayerId(),
                targetMeldId,
                selectedCards.getFirst().id()
        ));

        actionPresentationAdapter.handleActionResult(result, null);
    }


    private void replaceJokerInTargetMeld() {
        List<Card> selectedCards = hand.getSelectedCards();
        List<Card> naturalCandidates = selectedCards.stream()
                .filter(card -> !card.isJoker())
                .toList();

        if (naturalCandidates.isEmpty()) {
            System.out.println("Select a natural card to replace a joker. Selected cards: " + describeCards(selectedCards));
            return;
        }

        MeldId targetMeldId = hand.selectedTargetMeldId();

        if (targetMeldId == null) {
            System.out.println("Select a target meld first by clicking a played meld.");
            return;
        }

        ActionResult lastResult = null;

        for (Card candidate : naturalCandidates) {
            ActionResult result = gameController.apply(new ReplaceJokerInMeldAction(
                    gameController.state().roundState().activePlayerId(),
                    targetMeldId,
                    candidate.id()
            ));

            if (result.success()) {
                actionPresentationAdapter.handleActionResult(result, null);
                return;
            }

            lastResult = result;
        }

        if (naturalCandidates.size() > 1) {
            System.out.println("Tried " + naturalCandidates.size() + " selected natural cards, but none could replace a joker in the target meld. Selected cards: " + describeCards(selectedCards));
        }

        actionPresentationAdapter.handleActionResult(lastResult, null);
    }

    private String describeCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return "none";
        }

        return cards.stream()
                .map(Card::toString)
                .toList()
                .toString();
    }

    private void discardSelectedCard() {
        List<Card> selectedCards = hand.getSelectedCards();

        if (selectedCards.size() != 1) {
            System.out.println("Select exactly one card to discard.");
            return;
        }

        Card cardToDiscard = selectedCards.get(0);
        ActionResult result = gameController.apply(new DiscardAction(
                gameController.state().roundState().activePlayerId(),
                cardToDiscard.id()
        ));

        actionPresentationAdapter.handleActionResult(result, discardPilePosition());
    }

    private Point2D discardPilePosition() {
        return deckDiscardPanel.discardTopLeft();
    }

    private void addLayoutDebugOverlay() {
        addDebugArea(gameLayout.getHudArea(), Color.color(0.05, 0.05, 0.05, 0.45));
        addDebugArea(gameLayout.getOpponentPlayedArea(), Color.color(0.4, 0.1, 0.1, 0.25));
        addDebugArea(gameLayout.getPlayerPlayedArea(), Color.color(0.1, 0.1, 0.4, 0.25));
        addDebugArea(gameLayout.getPlayerHandArea(), Color.color(0.1, 0.4, 0.1, 0.25));
        addDebugArea(gameLayout.getButtonArea(), Color.color(0.4, 0.4, 0.1, 0.25));
    }

    private void addDebugArea(Rectangle2D area, Color color) {
        Rectangle rectangle = new Rectangle(area.getWidth(), area.getHeight());
        rectangle.setTranslateX(area.getMinX());
        rectangle.setTranslateY(area.getMinY());
        rectangle.setFill(color);
        rectangle.setStroke(Color.color(1, 1, 1, 0.25));
        rectangle.setMouseTransparent(true);

        FXGL.getGameScene().addUINode(rectangle);
    }
}
