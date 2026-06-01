package quetzal.cards;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Prototype visual prompt for a timed castigo decision.
 *
 * This is not yet connected to real turn state. It establishes the presentation
 * direction: a five-second decision window with a right-to-left countdown bar.
 */
public final class CastigoDecisionPrompt {

    public static final double DEFAULT_DECISION_SECONDS = 5.0;

    private final Group root = new Group();
    private final Rectangle background = new Rectangle(280, 70);
    private final Rectangle countdownBar = new Rectangle(280, 70);
    private final Text takeText = new Text("Take Castigo");
    private final Text passText = new Text("Pass");

    private Timeline timeline;

    public CastigoDecisionPrompt() {
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setFill(Color.color(0.06, 0.06, 0.06, 0.88));

        countdownBar.setArcWidth(12);
        countdownBar.setArcHeight(12);
        countdownBar.setFill(Color.color(0.65, 0.65, 0.65, 0.25));

        takeText.setFont(loadFont(16));
        takeText.setFill(Color.WHITE);
        takeText.setTranslateX(22);
        takeText.setTranslateY(42);

        passText.setFont(loadFont(16));
        passText.setFill(Color.WHITE);
        passText.setTranslateX(180);
        passText.setTranslateY(42);

        root.getChildren().addAll(background, countdownBar, takeText, passText);
        root.setVisible(false);
    }

    public Group root() {
        return root;
    }

    public void show(double x, double y, Runnable onTimeoutPass) {
        root.setTranslateX(x);
        root.setTranslateY(y);
        root.setVisible(true);
        countdownBar.setWidth(280);

        if (timeline != null) {
            timeline.stop();
        }

        long start = System.nanoTime();

        timeline = new Timeline(new KeyFrame(Duration.millis(33), event -> {
            double elapsed = (System.nanoTime() - start) / 1_000_000_000.0;
            double remainingRatio = Math.max(0, 1.0 - elapsed / DEFAULT_DECISION_SECONDS);
            countdownBar.setWidth(280 * remainingRatio);

            if (remainingRatio <= 0) {
                hide();

                if (onTimeoutPass != null) {
                    onTimeoutPass.run();
                }
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void hide() {
        if (timeline != null) {
            timeline.stop();
        }

        root.setVisible(false);
    }

    private Font loadFont(double size) {
        return Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), size);
    }
}
