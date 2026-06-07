package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class GameControls {

    private static final double BUTTON_SPACING = 10;
    private static final double BUTTON_AREA_PADDING_X = 40;
    private static final double BUTTON_AREA_PADDING_Y = 20;

    private final Hand hand;
    private final Rectangle2D buttonArea;
    private final HBox buttonBar;
    private final Runnable onDebugDrawer;
    private final Runnable onDiscard;
    private final Runnable onPlayMeld;
    private final Runnable onSortRank;
    private final Runnable onSortSuit;
    private final Runnable onRestoreCustomOrder;
    private final Runnable onSaveCustomOrder;

    public GameControls(GameLayout gameLayout, Hand hand, Runnable onDebugDrawer, Runnable onDiscard, Runnable onPlayMeld, Runnable onSortRank, Runnable onSortSuit, Runnable onRestoreCustomOrder, Runnable onSaveCustomOrder) {
        this.hand = hand;
        this.onDebugDrawer = onDebugDrawer == null ? () -> { } : onDebugDrawer;
        this.onDiscard = onDiscard == null ? () -> { } : onDiscard;
        this.onPlayMeld = onPlayMeld == null ? () -> { } : onPlayMeld;
        this.onSortRank = onSortRank == null ? () -> { } : onSortRank;
        this.onSortSuit = onSortSuit == null ? () -> { } : onSortSuit;
        this.onRestoreCustomOrder = onRestoreCustomOrder == null ? () -> { } : onRestoreCustomOrder;
        this.onSaveCustomOrder = onSaveCustomOrder == null ? () -> { } : onSaveCustomOrder;
        this.buttonArea = gameLayout.getButtonArea();
        this.buttonBar = createButtonBar();

        positionButtonBar();

        FXGL.getGameScene().addUINode(buttonBar);
    }

    public GameControls(GameLayout gameLayout, Hand hand, Runnable onDebugDrawer, Runnable onDiscard, Runnable onPlayMeld) {
        this(gameLayout, hand, onDebugDrawer, onDiscard, onPlayMeld, null, null, null, null);
    }

    public GameControls(GameLayout gameLayout, Hand hand, Runnable onDebugDrawer) {
        this(gameLayout, hand, onDebugDrawer, null, null, null, null, null, null);
    }

    public GameControls(GameLayout gameLayout, Hand hand) {
        this(gameLayout, hand, null, null, null, null, null, null, null);
    }

    private HBox createButtonBar() {
        Button playButton = gameButton(new Text("Play Hand"), Color.color(0.9, 0, 0));
        playButton.setOnAction(event -> onPlayMeld.run());

        Button discardButton = gameButton(new Text("Discard"), Color.color(0, 0.3, 0.9));
        discardButton.setOnAction(event -> onDiscard.run());

        Button sortRankButton = gameButton(new Text("Rank"), Color.color(0.8, 0.7, 0));
        sortRankButton.setOnAction(event -> onSortRank.run());

        Button sortSuitButton = gameButton(new Text("Suit"), Color.color(0.8, 0.7, 0));
        sortSuitButton.setOnAction(event -> onSortSuit.run());

        Button orderButton = gameButton(new Text("Order"), Color.color(0.55, 0.32, 0.7));
        wireOrderButton(orderButton);

        Button debugButton = gameButton(new Text("Debug"), Color.color(0.28, 0.22, 0.46));
        debugButton.setOnAction(event -> {
            playButtonConfirmation(debugButton);
            onDebugDrawer.run();
        });

        return new HBox(
                BUTTON_SPACING,
                playButton,
                discardButton,
                sortRankButton,
                sortSuitButton,
                orderButton,
                debugButton
        );
    }


    private void wireOrderButton(Button orderButton) {
        final boolean[] holdTriggered = {false};
        final PauseTransition holdTimer = new PauseTransition(Duration.seconds(0.65));

        holdTimer.setOnFinished(event -> {
            holdTriggered[0] = true;
            onSaveCustomOrder.run();
            playButtonConfirmation(orderButton);
        });

        orderButton.setOnMousePressed(event -> {
            holdTriggered[0] = false;
            holdTimer.playFromStart();
        });

        orderButton.setOnMouseReleased(event -> {
            holdTimer.stop();

            if (!holdTriggered[0]) {
                onRestoreCustomOrder.run();
            }
        });

        orderButton.setOnMouseExited(event -> {
            holdTimer.stop();
        });
    }

    private void positionButtonBar() {
        buttonBar.setTranslateX(buttonArea.getMinX() + BUTTON_AREA_PADDING_X);
        buttonBar.setTranslateY(buttonArea.getMinY() + BUTTON_AREA_PADDING_Y);
    }


    private void playButtonConfirmation(Button button) {
        ScaleTransition pop = new ScaleTransition(Duration.seconds(0.07), button);
        pop.setToX(1.10);
        pop.setToY(1.10);
        pop.setInterpolator(Interpolator.EASE_OUT);

        RotateTransition wiggleLeft = new RotateTransition(Duration.seconds(0.05), button);
        wiggleLeft.setToAngle(-3.0);
        wiggleLeft.setInterpolator(Interpolator.EASE_OUT);

        RotateTransition wiggleRight = new RotateTransition(Duration.seconds(0.05), button);
        wiggleRight.setToAngle(3.0);
        wiggleRight.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition settleScale = new ScaleTransition(Duration.seconds(0.10), button);
        settleScale.setToX(1.0);
        settleScale.setToY(1.0);
        settleScale.setInterpolator(Interpolator.EASE_IN);

        RotateTransition settleRotate = new RotateTransition(Duration.seconds(0.08), button);
        settleRotate.setToAngle(0.0);
        settleRotate.setInterpolator(Interpolator.EASE_IN);

        pop.setOnFinished(event -> wiggleLeft.play());
        wiggleLeft.setOnFinished(event -> wiggleRight.play());
        wiggleRight.setOnFinished(event -> {
            settleScale.play();
            settleRotate.play();
        });

        pop.play();
    }

    private Button gameButton(Text text, Color color) {
        Button button = new Button();

        text.setFont(Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 24));
        text.setStyle("-fx-fill: #ffffff;");

        button.setStyle(String.format("-fx-background-color: #%s ;", color.toString().substring(2, 8)));
        button.setPadding(new Insets(24));

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