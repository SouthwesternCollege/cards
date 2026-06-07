package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Development-only drawer for debug tools.
 *
 * This keeps prototype/debug controls out of the normal gameplay control row.
 */
public final class DebugDrawer {

    private static final double DRAWER_WIDTH = 230.0;
    private static final double TOP = 150.0;
    private static final double BUTTON_SPACING = 12.0;
    private static final double PADDING = 18.0;

    private final double sceneWidth;
    private final double sceneHeight;
    private final Runnable onTableView;
    private final Runnable onDebugHands;
    private final Runnable onAddKind;
    private final Runnable onAddRun;
    private final Runnable onStress;
    private final Runnable onClear;

    private Group root;
    private boolean visible = false;

    public DebugDrawer(
            double sceneWidth,
            double sceneHeight,
            Runnable onTableView,
            Runnable onDebugHands,
            Runnable onAddKind,
            Runnable onAddRun,
            Runnable onStress,
            Runnable onClear
    ) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.onTableView = onTableView == null ? () -> { } : onTableView;
        this.onDebugHands = onDebugHands == null ? () -> { } : onDebugHands;
        this.onAddKind = onAddKind == null ? () -> { } : onAddKind;
        this.onAddRun = onAddRun == null ? () -> { } : onAddRun;
        this.onStress = onStress == null ? () -> { } : onStress;
        this.onClear = onClear == null ? () -> { } : onClear;
    }

    public void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    public void show() {
        if (visible) {
            return;
        }

        visible = true;
        root = buildRoot();
        root.setTranslateX(sceneWidth);
        FXGL.getGameScene().addUINode(root);

        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.22), root);
        transition.setToX(sceneWidth - DRAWER_WIDTH);
        transition.setInterpolator(Interpolator.EASE_OUT);
        transition.play();
    }

    public void hide() {
        if (!visible) {
            return;
        }

        visible = false;

        if (root == null) {
            return;
        }

        Group closingRoot = root;
        root = null;

        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.16), closingRoot);
        transition.setToX(sceneWidth);
        transition.setInterpolator(Interpolator.EASE_IN);
        transition.setOnFinished(event -> FXGL.getGameScene().removeUINode(closingRoot));
        transition.play();
    }

    private Group buildRoot() {
        Group group = new Group();
        group.setTranslateY(TOP);

        Rectangle background = new Rectangle(DRAWER_WIDTH, sceneHeight - TOP - 40);
        background.setArcWidth(24);
        background.setArcHeight(24);
        background.setFill(Color.color(0.02, 0.02, 0.025, 0.92));
        background.setStroke(Color.color(1.0, 0.82, 0.18, 0.55));
        background.setStrokeWidth(2);
        background.setEffect(dropShadow(Color.BLACK, 6));

        Text title = new Text("DEBUG");
        title.setFont(loadFont(28));
        title.setFill(Color.GOLD);
        title.setEffect(dropShadow(Color.BLACK, 3));

        VBox buttons = new VBox(BUTTON_SPACING);
        buttons.setPadding(new Insets(PADDING));
        buttons.setTranslateX(0);
        buttons.setTranslateY(0);

        Button tableButton = debugButton("Table", Color.color(0.16, 0.16, 0.48), onTableView);
        Button handsButton = debugButton("Hands", Color.color(0.55, 0.38, 0.06), onDebugHands);
        Button kindButton = debugButton("+Kind", Color.color(0.25, 0.45, 0.25), onAddKind);
        Button runButton = debugButton("+Run", Color.color(0.25, 0.45, 0.45), onAddRun);
        Button stressButton = debugButton("Stress", Color.color(0.45, 0.25, 0.45), onStress);
        Button clearButton = debugButton("Clear", Color.color(0.25, 0.25, 0.25), onClear);

        buttons.getChildren().addAll(title, tableButton, handsButton, kindButton, runButton, stressButton, clearButton);
        group.getChildren().addAll(background, buttons);

        return group;
    }

    private Button debugButton(String textValue, Color color, Runnable action) {
        Button button = new Button();

        Text text = new Text(textValue);
        text.setFont(loadFont(22));
        text.setStyle("-fx-fill: #ffffff;");
        text.setEffect(dropShadow(color.darker().darker(), 2));

        button.setStyle(String.format("-fx-background-color: #%s ;", color.toString().substring(2, 8)));
        button.setPadding(new Insets(16));
        button.setPrefWidth(DRAWER_WIDTH - PADDING * 2);
        button.setEffect(dropShadow(Color.color(0.1, 0.1, 0.1), 5));
        button.setGraphic(text);
        button.setOnAction(event -> {
            playButtonPulse(button);
            action.run();
        });

        return button;
    }

    private void playButtonPulse(Button button) {
        ScaleTransition up = new ScaleTransition(Duration.seconds(0.06), button);
        up.setToX(1.06);
        up.setToY(1.06);
        up.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition down = new ScaleTransition(Duration.seconds(0.10), button);
        down.setToX(1.0);
        down.setToY(1.0);
        down.setInterpolator(Interpolator.EASE_IN);

        up.setOnFinished(event -> down.play());
        up.play();
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
