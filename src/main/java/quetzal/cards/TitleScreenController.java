package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Rough title/splash screen for Milestone 4E.
 *
 * Current behavior:
 * - full-screen dark background
 * - large LA KIKA title, approximately 25% of screen height
 * - title drops in from the top
 * - title fades out
 * - caller is notified when the animation completes
 */
public final class TitleScreenController {

    private static final double TITLE_HEIGHT_RATIO = 0.25;
    private static final double DROP_SECONDS = 2.2;
    private static final double HOLD_SECONDS = 0.8;
    private static final double FADE_SECONDS = 1.0;

    private final double sceneWidth;
    private final double sceneHeight;
    private final Group root = new Group();

    public TitleScreenController(double sceneWidth, double sceneHeight) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
    }

    public void show(Runnable onFinished) {
        Rectangle background = new Rectangle(sceneWidth, sceneHeight);
        background.setFill(Color.color(0.015, 0.045, 0.025));

        Text title = new Text("LA KIKA");
        title.setFont(loadFont(sceneHeight * TITLE_HEIGHT_RATIO));
        title.setFill(Color.WHITE);
        title.setEffect(titleShadow());

        double titleWidth = title.getLayoutBounds().getWidth();
        double titleHeight = title.getLayoutBounds().getHeight();
        double targetX = (sceneWidth - titleWidth) / 2.0;
        double targetY = sceneHeight * 0.42;
        double startY = -titleHeight - 60;

        title.setTranslateX(targetX);
        title.setTranslateY(startY);

        root.getChildren().addAll(background, title);
        FXGL.getGameScene().addUINode(root);

        FXGL.animationBuilder()
                .duration(Duration.seconds(DROP_SECONDS))
                .interpolator(Interpolators.SINE.EASE_OUT())
                .translate(title)
                .from(new Point2D(targetX, startY))
                .to(new Point2D(targetX, targetY))
                .buildAndPlay();

        FXGL.animationBuilder()
                .delay(Duration.seconds(DROP_SECONDS + HOLD_SECONDS))
                .duration(Duration.seconds(FADE_SECONDS))
                .fadeOut(root)
                .buildAndPlay();

        FXGL.runOnce(() -> {
            FXGL.getGameScene().removeUINode(root);

            if (onFinished != null) {
                onFinished.run();
            }
        }, Duration.seconds(DROP_SECONDS + HOLD_SECONDS + FADE_SECONDS));
    }

    private Font loadFont(double size) {
        return Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), size);
    }

    private DropShadow titleShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(2);
        shadow.setOffsetY(14.0);
        shadow.setColor(Color.color(0.05, 0.18, 0.05));
        return shadow;
    }
}
