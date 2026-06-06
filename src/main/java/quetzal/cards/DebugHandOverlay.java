package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;

/**
 * Development-only overlay that shows all player hands.
 *
 * This deliberately violates hot-seat privacy and must not be exposed as normal
 * gameplay. The real player-facing privacy solution will be a pass-device
 * screen after turn flow exists.
 */
public final class DebugHandOverlay {

    private static final double OUTER_PADDING = 34.0;
    private static final double ROW_GAP = 18.0;
    private static final double ROW_LABEL_WIDTH = 170.0;
    private static final int PLAYER_COUNT = 4;
    private static final int MOCK_HAND_SIZE = 13;

    private final double sceneWidth;
    private final double sceneHeight;
    private final CardViewFactory cardViewFactory = new CardViewFactory();
    private final MockHandFactory mockHandFactory = new MockHandFactory();

    private Group root;
    private boolean visible = false;

    public DebugHandOverlay(double sceneWidth, double sceneHeight) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
    }

    public void show() {
        if (visible) {
            return;
        }

        visible = true;
        root = buildRoot();
        FXGL.getGameScene().addUINode(root);
    }

    public void hide() {
        if (!visible) {
            return;
        }

        visible = false;

        if (root != null) {
            FXGL.getGameScene().removeUINode(root);
            root = null;
        }
    }

    public void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    private Group buildRoot() {
        Group group = new Group();

        Rectangle background = new Rectangle(sceneWidth, sceneHeight);
        background.setFill(Color.color(0.015, 0.015, 0.025, 0.96));
        background.setStroke(Color.color(1, 0.85, 0.18, 0.55));
        background.setStrokeWidth(3);

        Text title = new Text("DEBUG HAND OVERLAY");
        title.setFont(loadFont(38));
        title.setFill(Color.GOLD);
        title.setEffect(dropShadow(Color.BLACK, 4));
        title.setTranslateX(OUTER_PADDING);
        title.setTranslateY(58);

        Text subtitle = new Text("Development-only: all hands visible. Not gameplay.");
        subtitle.setFont(loadFont(18));
        subtitle.setFill(Color.color(0.82, 0.82, 0.82));
        subtitle.setEffect(dropShadow(Color.BLACK, 2));
        subtitle.setTranslateX(OUTER_PADDING);
        subtitle.setTranslateY(88);

        Button closeButton = gameButton("EXIT", Color.color(0.85, 0.12, 0.08));
        closeButton.setTranslateX(sceneWidth - 190);
        closeButton.setTranslateY(30);
        closeButton.setOnAction(event -> hide());

        group.getChildren().addAll(background, title, subtitle, closeButton);

        Map<PlayerId, List<Card>> mockHands = mockHandFactory.createMockHands(PLAYER_COUNT, MOCK_HAND_SIZE);
        addHandRows(group, mockHands);

        return group;
    }

    private void addHandRows(Group group, Map<PlayerId, List<Card>> hands) {
        double top = 125.0;
        double availableHeight = sceneHeight - top - OUTER_PADDING;
        double rowHeight = (availableHeight - ROW_GAP * (hands.size() - 1)) / hands.size();
        double rowWidth = sceneWidth - OUTER_PADDING * 2;

        for (int i = 1; i <= hands.size(); i++) {
            PlayerId playerId = new PlayerId(i);
            double rowY = top + (i - 1) * (rowHeight + ROW_GAP);

            Rectangle rowBackground = new Rectangle(rowWidth, rowHeight);
            rowBackground.setArcWidth(18);
            rowBackground.setArcHeight(18);
            rowBackground.setTranslateX(OUTER_PADDING);
            rowBackground.setTranslateY(rowY);
            rowBackground.setFill(Color.color(0.03, 0.03, 0.04, 0.72));
            rowBackground.setStroke(PlayerColorPalette.colorFor(playerId));
            rowBackground.setStrokeWidth(2);
            rowBackground.setEffect(dropShadow(Color.BLACK, 4));

            Text label = new Text("Player " + i);
            label.setFont(loadFont(26));
            label.setFill(PlayerColorPalette.colorFor(playerId));
            label.setEffect(dropShadow(Color.BLACK, 3));
            label.setTranslateX(OUTER_PADDING + 28);
            label.setTranslateY(rowY + 48);

            Text warning = new Text("DEBUG");
            warning.setFont(loadFont(16));
            warning.setFill(Color.GOLD);
            warning.setEffect(dropShadow(Color.BLACK, 2));
            warning.setTranslateX(OUTER_PADDING + 28);
            warning.setTranslateY(rowY + 78);

            group.getChildren().addAll(rowBackground, label, warning);

            Rectangle2D contentArea = new Rectangle2D(
                    OUTER_PADDING + ROW_LABEL_WIDTH,
                    rowY + 12,
                    rowWidth - ROW_LABEL_WIDTH - 24,
                    rowHeight - 24
            );

            addHandCards(group, hands.get(playerId), contentArea);
        }
    }

    private void addHandCards(Group group, List<Card> cards, Rectangle2D area) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        double spacing = handSpacing(cards.size(), area);
        double totalWidth = CardViewMetrics.renderedWidth() + spacing * (cards.size() - 1);
        double startX = area.getMinX() + (area.getWidth() - totalWidth) / 2.0;
        double y = LayoutRegionMath.centeredCardY(area);

        for (int i = 0; i < cards.size(); i++) {
            Node cardView = cardViewFactory.createView(cards.get(i));
            cardView.setTranslateX(startX + i * spacing);
            cardView.setTranslateY(y);
            cardView.setMouseTransparent(true);
            group.getChildren().add(cardView);
        }
    }

    private double handSpacing(int cardCount, Rectangle2D area) {
        if (cardCount <= 1) {
            return CardViewMetrics.renderedWidth();
        }

        double availableWidth = area.getWidth() - CardViewMetrics.renderedWidth();
        double idealSpacing = availableWidth / (cardCount - 1);
        return Math.max(CardViewMetrics.minVisibleCardSpacing(), Math.min(80.0, idealSpacing));
    }

    private Button gameButton(String textValue, Color color) {
        Button button = new Button();

        Text text = new Text(textValue);
        text.setFont(loadFont(24));
        text.setStyle("-fx-fill: #ffffff;");

        button.setStyle(String.format("-fx-background-color: #%s ;", color.toString().substring(2, 8)));
        button.setPrefWidth(150);
        button.setPrefHeight(64);

        text.setEffect(dropShadow(color.darker().darker(), 2));
        button.setEffect(dropShadow(Color.color(0.1, 0.1, 0.1), 6));

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
