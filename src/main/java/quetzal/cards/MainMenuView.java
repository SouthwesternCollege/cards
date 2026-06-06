package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Rough main menu for Milestone 4E.
 *
 * The menu is intentionally simple. Settings and Rules are placeholders until
 * those screens become real milestones.
 */
public final class MainMenuView {

    private final double sceneWidth;
    private final double sceneHeight;
    private final Runnable onPlay;
    private final Group root = new Group();
    private final Text statusText = new Text("");

    public MainMenuView(double sceneWidth, double sceneHeight, Runnable onPlay) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.onPlay = onPlay;
    }

    public void show() {
        Rectangle background = new Rectangle(sceneWidth, sceneHeight);
        background.setFill(Color.color(0.02, 0.045, 0.025));

        Text title = new Text("LA KIKA");
        title.setFont(loadFont(86));
        title.setFill(Color.WHITE);
        title.setEffect(dropShadow(Color.color(0.05, 0.18, 0.05), 8));
        title.setTranslateX((sceneWidth - title.getLayoutBounds().getWidth()) / 2.0);
        title.setTranslateY(sceneHeight * 0.25);

        Button playButton = menuButton("PLAY", Color.color(0.85, 0.05, 0.05));
        playButton.setOnAction(event -> {
            hide();

            if (onPlay != null) {
                onPlay.run();
            }
        });

        Button settingsButton = menuButton("SETTINGS", Color.color(0.12, 0.28, 0.85));
        settingsButton.setOnAction(event -> setStatus("Settings coming soon"));

        Button rulesButton = menuButton("RULES", Color.color(0.78, 0.62, 0.08));
        rulesButton.setOnAction(event -> setStatus("Rules coming soon"));

        VBox buttonBox = new VBox(18, playButton, settingsButton, rulesButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setTranslateX((sceneWidth - 320) / 2.0);
        buttonBox.setTranslateY(sceneHeight * 0.40);

        statusText.setFont(loadFont(22));
        statusText.setFill(Color.color(0.82, 0.82, 0.82));
        statusText.setEffect(dropShadow(Color.BLACK, 2));
        statusText.setTranslateX(sceneWidth * 0.5 - 170);
        statusText.setTranslateY(sceneHeight * 0.78);

        root.getChildren().addAll(background, title, buttonBox, statusText);
        FXGL.getGameScene().addUINode(root);
    }

    public void hide() {
        FXGL.getGameScene().removeUINode(root);
    }

    private void setStatus(String status) {
        statusText.setText(status == null ? "" : status);
    }

    private Button menuButton(String label, Color color) {
        Button button = new Button();
        button.setPrefWidth(320);
        button.setPadding(new Insets(22));
        button.setStyle(String.format("-fx-background-color: #%s; -fx-background-radius: 8;", color.toString().substring(2, 8)));
        button.setEffect(dropShadow(Color.color(0.02, 0.02, 0.02), 6));

        Text text = new Text(label);
        text.setFont(loadFont(32));
        text.setFill(Color.WHITE);
        text.setEffect(dropShadow(color.darker().darker(), 2));
        button.setGraphic(text);

        return button;
    }

    private Font loadFont(double size) {
        return Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), size);
    }

    private DropShadow dropShadow(Color color, double offsetY) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(1);
        shadow.setOffsetY(offsetY);
        shadow.setColor(color);
        return shadow;
    }
}
