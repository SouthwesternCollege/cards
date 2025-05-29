package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.CursorInfo;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.almasb.fxgl.texture.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        gameSettings.setDefaultCursor(new CursorInfo("cursor.png", 0, 0));
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
        title.setFont(font);
        title.setStyle("-fx-fill: white;");

        // Drop shadow effect on text
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

        // Animate the text by moving it to the target position
        FXGL.animationBuilder()
                .duration(Duration.seconds(4))
                .autoReverse(true)
                .interpolator(Interpolators.SINE.EASE_IN_OUT())
                .translate(title)
                .from(new Point2D(WIDTH / 2 - 200, -200)) // Starting position
                .to(new Point2D(WIDTH / 2 - 200, 300)) // Target position
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

//        try {
//            System.out.println(getClass().getResource("../../assets/shaders/crt.glsl"));
//            String shader = Files.readString(Paths.get(getClass().getResource("../../assets/shaders/crt.glsl").toURI()));
//            ImageView view = new ImageView();
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }

    }

    @Override
    protected void initUI() {
        // Create a 'Play' button
        Button playButton = gameButton(new Text("Play Hand"), Color.color(0.9, 0, 0));

        // Attach the playSelectedCards() method to the button's action
        playButton.setOnAction(event -> hand.playSelectedCards());

        Button discardButton = gameButton(new Text("Discard"), Color.color(0, 0.3, 0.9));
        Button sortRankButton = gameButton(new Text("Rank"), Color.color(0.8, 0.7, 0));
        Button sortSuitButton = gameButton(new Text("Suit"), Color.color(0.8, 0.7, 0));

        // Add the buttons to the game's UI
        HBox buttons = new HBox(10, playButton, discardButton, sortSuitButton, sortRankButton);
        buttons.setTranslateX(WIDTH/2-100);
        buttons.setTranslateY(HEIGHT-100);


        // Can't get these fuckin shaders to work
        FXGL.getGameScene().addUINode(buttons);




    }

    private Button gameButton(Text text, Color color) {
        Button button = new Button();


        text.setFont(Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 24));
        text.setStyle("-fx-fill: #ffffff;");
        button.setStyle(String.format("-fx-background-color: #%s ;", color.toString().substring(2, 8)));
        button.setPadding(new Insets(24));

        // Dropshadow effect on text
        DropShadow textShadow = new DropShadow();
        textShadow.setRadius(1);
        textShadow.setOffsetY(2.0);
        textShadow.setColor(color.darker().darker());
        text.setEffect(textShadow);

        //Dropshadow effect on button
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setRadius(1);
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.color(0.1, 0.1, 0.1));
        button.setEffect(buttonShadow);

        button.setGraphic(text);

        return button;
    }

}