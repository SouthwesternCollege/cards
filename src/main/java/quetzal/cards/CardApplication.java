package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.CursorInfo;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;


public class CardApplication extends GameApplication {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private Hand hand;
    private GameLayout gameLayout;
    private GameHUD gameHUD;
    private GameControls gameControls;
    private final Font font = Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 36);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Cards");
        gameSettings.setAppIcon("icon.png");
        gameSettings.setVersion("0.0β");
        gameSettings.setFullScreenAllowed(true);  // Allow full-screen mode
        gameSettings.setFullScreenFromStart(true);       // Set the app to start in full-screen mode
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
        // Title Screen
        Text title = new Text("LA KIKA");
        title.setFont(font);
        title.setStyle("-fx-fill: white;");

        // Drop shadow effect on text
        DropShadow textShadow = new DropShadow();
        textShadow.setRadius(1);
        textShadow.setOffsetY(10.0);
        textShadow.setColor(Color.color(0.1, 0.3, 0.1));
        title.setEffect(textShadow);

        // Set initial position
        title.setTranslateX((double) WIDTH / 2);
        title.setTranslateY(0);

        // Add the text to the game scene
        FXGL.getGameScene().addUINode(title);

        // Animate the text by moving it to the target position
        FXGL.animationBuilder()
                .duration(Duration.seconds(4))
                .autoReverse(true)
                .interpolator(Interpolators.SINE.EASE_IN_OUT())
                .translate(title)
                .from(new Point2D((double) WIDTH / 2 - 200, -200)) // Starting position
                .to(new Point2D((double) WIDTH / 2 - 200, 300)) // Target position
                .buildAndPlay();

        // Add a delay to keep the title visible for 3 more seconds after the animation ends
        FXGL.animationBuilder()
                .delay(Duration.seconds(4))
                .duration(Duration.seconds(3))
                .fadeOut(title) // Apply fade-out effect
                .buildAndPlay(); // Build and play the animation

        FXGL.runOnce(() -> {
            FXGL.getGameScene().removeUINode(title); // Remove title after delay
        }, Duration.seconds(7));

        // Background music
        FXGL.loopBGM("theme.mp3");

        FXGL.getGameWorld().addEntityFactory(new GameFactory());
        FXGL.spawn("Background", new SpawnData(0, 0).put("width", WIDTH).put("height", HEIGHT));

        // Overlay to help with debugging GameLayout
        addLayoutDebugOverlay();

        // Initialize gameLayout
        gameHUD = new GameHUD(gameLayout);

        // Create a Deck and shuffle
        Deck deck = Deck.laKikaPrototypeDeck();
        deck.shuffle();

        // Initialize hand area
        Rectangle2D playerHandArea = gameLayout.getPlayerHandArea();

        // Create the hand inside the player hand area
        hand = new Hand(playerHandArea,deck);

        // Number of cards in the hand
        int handSize = 13;
        hand.populateHand(handSize);

    }

    @Override
    protected void initUI() {
        gameControls = new GameControls(gameLayout, hand);

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