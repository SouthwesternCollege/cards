package quetzal.cards;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardApplication extends GameApplication {
    private static int WIDTH = 1920;
    private static int HEIGHT = 1080;

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
        // Load and play background music in a loop

        FXGL.loopBGM("theme.mp3");

        // Create volume control slider
        Slider volumeSlider = new Slider(0, 1, FXGL.getSettings().getGlobalMusicVolume()); // range from 0 to 1
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);

        // Update volume when the slider is moved
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            FXGL.getSettings().setGlobalMusicVolume(newVal.doubleValue());
        });

        // Add the slider to the game's UI
        VBox volumeControlBox = new VBox(10, new Text("Music Volume"), volumeSlider);
        volumeControlBox.setTranslateX(50);
        volumeControlBox.setTranslateY(50);
        FXGL.getGameScene().addUINode(volumeControlBox);
        
        // 2 - A mapped to 0 - 12
        int handSize = 13;
        FXGL.getGameWorld().addEntityFactory(new CardFactory());
        FXGL.spawn("Background", new SpawnData(0, 0).put("width", WIDTH).put("height", HEIGHT));

        List<Entity> hand = new ArrayList<>();
        List<Entity> selected = new ArrayList<>();

        for (int i = 0; i < handSize; i++) {
            int index = new Random().nextInt(0, 52);
            Entity cardEntity = FXGL.spawn("Card", new SpawnData((WIDTH*2/5-72)/(handSize - 1) * i + WIDTH/3, HEIGHT / 2, i)
                    .put("zIndex", i)
                    .put("card-index", index)
                    .put("hand", hand)
                    .put("selected", selected));
            hand.add(cardEntity);
        }
    }
}