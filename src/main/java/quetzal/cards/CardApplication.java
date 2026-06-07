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
import org.jetbrains.annotations.NotNull;

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
    private GameController gameController;
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
                gameLayout.getPlayerPlayedArea(),
                deck,
                selectionFeedback,
                handChangeListener
        );

        hand.populateHand(gameController.handFor(localPlayerId));

        fullPlayAreaView = new FullPlayAreaView(WIDTH, HEIGHT);
        debugHandOverlay = new DebugHandOverlay(WIDTH, HEIGHT);
        gameControls = new GameControls(gameLayout, hand, fullPlayAreaView::toggle, debugHandOverlay::toggle, this::discardSelectedCard);

        deckDiscardPanel = new DeckDiscardPanel(gameLayout, deck, new DeckDiscardActions() {
            @Override
            public void drawFromDeck(Point2D sourcePosition) {
                ActionResult result = gameController.apply(new DrawFromDeckAction(gameController.state().roundState().activePlayerId()));
                handleActionResult(result, sourcePosition);
                deckDiscardPanel.refresh();
            }

            @Override
            public void takeCastigo(Point2D sourcePosition) {
                // Prototype placeholder. Real castigo resolution belongs to GameState / TurnController.
                hand.drawOneCardFromDeck(sourcePosition);
            }

            @Override
            public void passCastigo() {
                // Prototype placeholder for out-of-turn castigo prompts.
            }
        });
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

        handleActionResult(result, discardPilePosition());
        deckDiscardPanel.refresh();
    }

    private Point2D discardPilePosition() {
        return deckDiscardPanel.discardTopLeft();
    }

    private void handleActionResult(ActionResult result, Point2D sourcePosition) {
        if (result == null) {
            return;
        }

        if (!result.success()) {
            System.out.println("Action failed: " + result.message());
            return;
        }

        for (GameEvent event : result.events()) {
            handleGameEvent(event, sourcePosition);
        }

        gameHudController.refreshFromGameState(gameController.state());
    }

    private void handleGameEvent(GameEvent event, Point2D sourcePosition) {
        if (event instanceof CardDrawnEvent cardDrawnEvent) {
            PlayerId visiblePlayerId = gameController.state().roundState().activePlayerId();

            if (cardDrawnEvent.playerId().equals(visiblePlayerId)) {
                hand.addCardFromSource(cardDrawnEvent.card(), sourcePosition);
            }
        }

        if (event instanceof CardDiscardedEvent cardDiscardedEvent) {
            PlayerId visiblePlayerId = gameController.state().roundState().activePlayerId();

            if (cardDiscardedEvent.playerId().equals(visiblePlayerId)) {
                hand.discardSelectedCardVisual(cardDiscardedEvent.card(), sourcePosition);
            }

            deckDiscardPanel.setTopDiscardCard(cardDiscardedEvent.card());
        }
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
