package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class CardApplication extends GameApplication {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private Hand hand;
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
        gameSettings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newGameMenu() {
                return new PauseMenu();
            }
        });
    }

    @Override
    protected void initGame() {

        Text title = new Text("LA KIKA");
        title.setFont(Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 100));
        title.setStyle("-fx-fill: white;");
        // Dropshadow effect on text
        DropShadow textShadow = new DropShadow();
        textShadow.setRadius(1);
        textShadow.setOffsetY(10.0);
        textShadow.setColor(Color.color(0.1, 0.3, 0.1));
        title.setEffect(textShadow);

        // Set initial position
        title.setTranslateX(WIDTH / 2);
        title.setTranslateY(0);

        // Add the text to the game scene
        FXGL.getGameScene().addUINode(title);


        // Animate the text by moving it to the right
        FXGL.animationBuilder()
                .duration(Duration.seconds(4))
                .onFinished(() -> FXGL.getGameScene().removeUINode(title))
                .autoReverse(true) // Moves back after reaching the end
                .interpolator(Interpolators.SINE.EASE_IN_OUT())
                .translate(title)
                .from(new Point2D(WIDTH / 2 - 200, -200)) // Starting position
                .to(new Point2D(WIDTH / 2 - 200, 300)) // Target position
                .buildAndPlay();

        // Background music
        FXGL.loopBGM("theme.mp3");

        // Create volume control slider
        Slider volumeSlider = new Slider(0, 1, FXGL.getSettings().getGlobalMusicVolume()); // range from 0 to 1
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);

        // Update volume when the slider is moved
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            FXGL.getSettings().setGlobalMusicVolume(newVal.doubleValue());
        });

        VBox volumeControlBox = new VBox(10, new Text("Music Volume"), volumeSlider);
        volumeControlBox.setTranslateX(50);
        volumeControlBox.setTranslateY(50);

        FXGL.getGameScene().addUINode(volumeControlBox);


        FXGL.getGameWorld().addEntityFactory(new CardFactory());
        FXGL.spawn("Background", new SpawnData(0, 0).put("width", WIDTH).put("height", HEIGHT));

        // Create a Deck and shuffle
        Deck deck = new Deck();
        deck.shuffle();

        // Create the hand with appropriate card spacing
        hand = new Hand(WIDTH / 3, HEIGHT * 2 / 3, deck);

        // Number of cards in the hand
        int handSize = 13;
        hand.populateHand(handSize);

    }

    @Override
    protected void initUI() {
        // Create a 'Play' button
        Text buttonText = new Text("Play Hand");
        Button playButton = new Button();

        buttonText.setFont(Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 24));
        buttonText.setStyle("-fx-fill: #ffffff;");
        playButton.setStyle("-fx-background-color: #ff0000;");
        playButton.setPadding(new Insets(24));

        // Dropshadow effect on text
        DropShadow textShadow = new DropShadow();
        textShadow.setRadius(1);
        textShadow.setOffsetY(2.0);
        textShadow.setColor(Color.color(.6, 0, 0));
        buttonText.setEffect(textShadow);

        //Dropshadow effect on button
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setRadius(1);
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.color(0.1, 0.1, 0.1));
        playButton.setEffect(buttonShadow);

        playButton.setGraphic(buttonText);

        // Set button position on the screen
        playButton.setTranslateX(WIDTH / 2);  // Adjust X position
        playButton.setTranslateY(HEIGHT - 100);  // Adjust Y position

        // Attach the playSelectedCards() method to the button's action
        playButton.setOnAction(event -> {
            hand.playSelectedCards();
        });

        // Add the button to the game's UI
        FXGL.getGameScene().addUINode(playButton);
    }

}