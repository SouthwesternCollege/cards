package quetzal.cards;

import com.almasb.fxgl.animation.Animation;
import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.core.util.EmptyRunnable;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontType;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PauseMenu extends FXGLMenu {

    private static final int SIZE = 150;

    private Animation<?> animation;

    public PauseMenu() {
        super(MenuType.GAME_MENU);

        getContentRoot().setTranslateX(FXGL.getAppWidth() / 2.0 - SIZE);
        getContentRoot().setTranslateY(FXGL.getAppHeight() / 2.0 - SIZE);

        var shape = Shape.subtract(new Circle(SIZE, SIZE, SIZE), new Rectangle(0, SIZE, SIZE*2, SIZE));


        var shape2 = Shape.subtract(shape, new Rectangle(0, 0, SIZE, SIZE));

        var shape3 = new Rectangle(SIZE*2, SIZE / 2);

        shape = Shape.subtract(shape, new Rectangle(SIZE, 0, SIZE, SIZE));


        shape.setStrokeWidth(3);
        shape.strokeProperty().bind(
                Bindings.when(shape.hoverProperty()).then(Color.BLUE).otherwise(Color.BLACK)
        );

        shape.fillProperty().bind(
                Bindings.when(shape.pressedProperty()).then(Color.BLUE).otherwise(Color.color(0.1, 0.05, 0.0, 0.75))
        );
        shape.setOnMouseClicked(e -> fireResume());

        shape2.setStrokeWidth(3);
        shape2.strokeProperty().bind(
                Bindings.when(shape2.hoverProperty()).then(Color.RED).otherwise(Color.BLACK)
        );

        shape2.fillProperty().bind(
                Bindings.when(shape2.pressedProperty()).then(Color.RED).otherwise(Color.color(0.1, 0.05, 0.0, 0.75))
        );
        shape2.setOnMouseClicked(e -> FXGL.getGameController().exit());


        shape3.setStrokeWidth(3);
        shape3.strokeProperty().bind(
                Bindings.when(shape3.hoverProperty()).then(Color.YELLOW).otherwise(Color.BLACK)
        );

        shape3.fillProperty().bind(
                Bindings.when(shape3.pressedProperty()).then(Color.YELLOW).otherwise(Color.color(0.1, 0.05, 0.0, 0.75))
        );

        shape3.setTranslateY(SIZE);

        Text textResume = FXGL.getUIFactoryService().newText("RESUME", Color.WHITE, FontType.GAME, 24.0);
        textResume.setTranslateX(50);
        textResume.setTranslateY(100);
        textResume.setMouseTransparent(true);

        Text textExit = FXGL.getUIFactoryService().newText("EXIT", Color.WHITE, FontType.GAME, 24.0);
        textExit.setTranslateX(200);
        textExit.setTranslateY(100);
        textExit.setMouseTransparent(true);

        Text textOptions = FXGL.getUIFactoryService().newText("OPTIONS", Color.WHITE, FontType.GAME, 24.0);
        textOptions.setTranslateX(110);
        textOptions.setTranslateY(195);
        textOptions.setMouseTransparent(true);

        // Create volume control slider
        Slider volumeSlider = new Slider(0, 1, FXGL.getSettings().getGlobalMusicVolume()); // range from 0 to 1
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);

        // Update volume when the slider is moved
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            FXGL.getSettings().setGlobalMusicVolume(newVal.doubleValue());
        });

        Text volumeText = FXGL.getUIFactoryService().newText("MUSIC VOLUME", Color.WHITE, FontType.GAME, 24.0);

        VBox volumeControlBox = new VBox(10, volumeText, volumeSlider);
        volumeControlBox.setTranslateX(50);
        volumeControlBox.setTranslateY(250);



        getContentRoot().getChildren().addAll(shape, shape2, shape3, textResume, textExit, textOptions, volumeControlBox);


        getContentRoot().setScaleX(0);
        getContentRoot().setScaleY(0);

        animation = FXGL.animationBuilder()
                .duration(Duration.seconds(0.66))
                .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                .scale(getContentRoot())
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
}