package quetzal.cards;

import com.almasb.fxgl.animation.Animation;
import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.core.util.EmptyRunnable;
import com.almasb.fxgl.dsl.FXGL;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PauseMenu extends FXGLMenu {

    private final Animation<?> animation;

    public PauseMenu() {
        super(MenuType.GAME_MENU);
        Button resumeButton = gameButton("RESUME", Color.DODGERBLUE);
        resumeButton.setOnAction(e -> fireResume());

        Button optionsButton = gameButton("OPTIONS", Color.GOLD);

        Button exitButton = gameButton("EXIT", Color.CRIMSON);
        exitButton.setOnAction(e -> FXGL.getGameController().exit());

        // Volume slider
        Slider volumeSlider = new Slider(0, 1, FXGL.getSettings().getGlobalMusicVolume());
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setShowTickMarks(false);

        volumeSlider.setStyle("""
                    -fx-control-inner-background: #202020;
                    -fx-accent: #f4c542;
                """);

        Text volumeText = new Text("MUSIC VOLUME");
        volumeText.setFont(Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 24));
        volumeText.setFill(Color.WHITE);

        VBox volumeBox = new VBox(10, volumeText, volumeSlider);
        volumeBox.setPadding(new Insets(18));
        volumeBox.setStyle("""
                    -fx-background-color: #3a2b55;
                    -fx-background-radius: 8;
                    -fx-border-color: black;
                    -fx-border-width: 3;
                    -fx-border-radius: 8;
                """);

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> FXGL.getSettings().setGlobalMusicVolume(newVal.doubleValue()));

        VBox menuBox = new VBox(16);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(320);

        resumeButton.setMaxWidth(Double.MAX_VALUE);
        optionsButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setMaxWidth(Double.MAX_VALUE);

        menuBox.getChildren().addAll(
                resumeButton,
                optionsButton,
                volumeBox,
                exitButton
        );

        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        overlay.setFill(Color.color(0, 0, 0, 0.55));
        overlay.setMouseTransparent(false);

        StackPane pauseRoot = new StackPane();
        pauseRoot.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        pauseRoot.setAlignment(Pos.CENTER);
        pauseRoot.getChildren().addAll(overlay, menuBox);

        getContentRoot().getChildren().add(pauseRoot);

        animation = FXGL.animationBuilder()
                .duration(Duration.seconds(0.66))
                .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                .scale(menuBox)
                .from(new Point2D(0, 0))
                .to(new Point2D(1, 1))
                .build();
    }

    @Override
    public void onCreate() {
        animation.setOnFinished(EmptyRunnable.INSTANCE);
        animation.stop();
        animation.start();
    }

    @Override
    protected void onUpdate(double tpf) {
        animation.onUpdate(tpf);
    }

    public Button gameButton(String label, Color color) {
        Text text = new Text(label);

        text.setFont(Font.loadFont(
                getClass().getResourceAsStream("/DePixelHalbfett.ttf"),
                24
        ));
        text.setStyle("-fx-fill: #ffffff;");

        Button button = new Button();
        button.setStyle(String.format(
                "-fx-background-color: #%s;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: rgba(0,0,0,0.8);" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 8;",
                color.toString().substring(2, 8)
        ));

        button.setPadding(new Insets(18, 28, 18, 28));

        DropShadow textShadow = new DropShadow();
        textShadow.setRadius(1);
        textShadow.setOffsetY(2.0);
        textShadow.setColor(color.darker().darker());
        text.setEffect(textShadow);

        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setRadius(1);
        buttonShadow.setOffsetY(6.0);
        buttonShadow.setColor(Color.color(0.1, 0.1, 0.1));
        button.setEffect(buttonShadow);

        button.setGraphic(text);
        return button;
    }
}