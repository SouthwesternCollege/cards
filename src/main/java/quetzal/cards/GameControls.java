package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GameControls {

    private static final double BUTTON_SPACING = 10;
    private static final double BUTTON_AREA_PADDING_X = 40;
    private static final double BUTTON_AREA_PADDING_Y = 20;

    private final Hand hand;
    private final Rectangle2D buttonArea;
    private final HBox buttonBar;
    private final Runnable onTableView;
    private final Runnable onDebugHands;

    public GameControls(GameLayout gameLayout, Hand hand, Runnable onTableView, Runnable onDebugHands) {
        this.hand = hand;
        this.onTableView = onTableView == null ? () -> { } : onTableView;
        this.onDebugHands = onDebugHands == null ? () -> { } : onDebugHands;
        this.buttonArea = gameLayout.getButtonArea();
        this.buttonBar = createButtonBar();

        positionButtonBar();

        FXGL.getGameScene().addUINode(buttonBar);
    }

    public GameControls(GameLayout gameLayout, Hand hand, Runnable onTableView) {
        this(gameLayout, hand, onTableView, null);
    }

    public GameControls(GameLayout gameLayout, Hand hand) {
        this(gameLayout, hand, null, null);
    }

    private HBox createButtonBar() {
        Button playButton = gameButton(new Text("Play Hand"), Color.color(0.9, 0, 0));
        playButton.setOnAction(event -> hand.playSelectedCards());

        Button discardButton = gameButton(new Text("Discard"), Color.color(0, 0.3, 0.9));

        Button sortRankButton = gameButton(new Text("Rank"), Color.color(0.8, 0.7, 0));
        sortRankButton.setOnAction(event -> hand.sortByRank());

        Button sortSuitButton = gameButton(new Text("Suit"), Color.color(0.8, 0.7, 0));
        sortSuitButton.setOnAction(event -> hand.sortBySuit());

        Button addKindTestButton = gameButton(new Text("+Kind"), Color.color(0.25, 0.45, 0.25));
        addKindTestButton.setOnAction(event -> hand.debugAddKindMeld());

        Button addStraightTestButton = gameButton(new Text("+Run"), Color.color(0.25, 0.45, 0.45));
        addStraightTestButton.setOnAction(event -> hand.debugAddStraightFlushMeld());

        Button stressMeldsButton = gameButton(new Text("Stress"), Color.color(0.45, 0.25, 0.45));
        stressMeldsButton.setOnAction(event -> hand.debugAddManyMelds());

        Button clearMeldsButton = gameButton(new Text("Clear"), Color.color(0.25, 0.25, 0.25));
        clearMeldsButton.setOnAction(event -> hand.debugClearVisualMelds());

        Button tableButton = gameButton(new Text("Table"), Color.color(0.16, 0.16, 0.48));
        tableButton.setOnAction(event -> onTableView.run());

        Button debugHandsButton = gameButton(new Text("Hands"), Color.color(0.55, 0.38, 0.06));
        debugHandsButton.setOnAction(event -> onDebugHands.run());

        return new HBox(
                BUTTON_SPACING,
                playButton,
                discardButton,
                sortRankButton,
                sortSuitButton,
                addKindTestButton,
                addStraightTestButton,
                stressMeldsButton,
                clearMeldsButton,
                tableButton,
                debugHandsButton
        );
    }

    private void positionButtonBar() {
        buttonBar.setTranslateX(buttonArea.getMinX() + BUTTON_AREA_PADDING_X);
        buttonBar.setTranslateY(buttonArea.getMinY() + BUTTON_AREA_PADDING_Y);
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