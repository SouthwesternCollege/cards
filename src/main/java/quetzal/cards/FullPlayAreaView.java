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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Read-only full table view for inspecting every player's played melds.
 *
 * Milestone 4G intentionally uses mock data because real multiplayer GameState
 * and PlayArea state do not exist yet.
 */
public final class FullPlayAreaView {

    private static final double OUTER_PADDING = 34.0;
    private static final double ROW_GAP = 18.0;
    private static final double ROW_LABEL_WIDTH = 170.0;

    private final double sceneWidth;
    private final double sceneHeight;
    private final CardViewFactory cardViewFactory = new CardViewFactory();
    private final MockPlayAreaFactory mockPlayAreaFactory = new MockPlayAreaFactory();

    private Group root;
    private boolean visible = false;

    public FullPlayAreaView(double sceneWidth, double sceneHeight) {
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
        background.setFill(Color.color(0.01, 0.025, 0.015, 0.96));
        background.setStroke(Color.color(1, 1, 1, 0.18));
        background.setStrokeWidth(3);

        Text title = new Text("FULL TABLE VIEW");
        title.setFont(loadFont(38));
        title.setFill(Color.WHITE);
        title.setEffect(dropShadow(Color.BLACK, 4));
        title.setTranslateX(OUTER_PADDING);
        title.setTranslateY(58);

        Text subtitle = new Text("Read-only mock play area");
        subtitle.setFont(loadFont(18));
        subtitle.setFill(Color.color(0.72, 0.72, 0.72));
        subtitle.setEffect(dropShadow(Color.BLACK, 2));
        subtitle.setTranslateX(OUTER_PADDING);
        subtitle.setTranslateY(88);

        Button closeButton = gameButton("EXIT", Color.color(0.85, 0.12, 0.08));
        closeButton.setTranslateX(sceneWidth - 190);
        closeButton.setTranslateY(30);
        closeButton.setOnAction(event -> hide());

        group.getChildren().addAll(background, title, subtitle, closeButton);

        Map<PlayerId, List<VisualMeld>> mockMelds = mockPlayAreaFactory.createMockMelds(4);
        addPlayerRows(group, mockMelds);

        return group;
    }

    private void addPlayerRows(Group group, Map<PlayerId, List<VisualMeld>> mockMelds) {
        int playerCount = mockMelds.size();
        double top = 125.0;
        double availableHeight = sceneHeight - top - OUTER_PADDING;
        double rowHeight = (availableHeight - ROW_GAP * (playerCount - 1)) / playerCount;
        double rowWidth = sceneWidth - OUTER_PADDING * 2;

        for (int i = 1; i <= playerCount; i++) {
            PlayerId playerId = new PlayerId(i);
            double rowY = top + (i - 1) * (rowHeight + ROW_GAP);

            Rectangle rowBackground = new Rectangle(rowWidth, rowHeight);
            rowBackground.setArcWidth(18);
            rowBackground.setArcHeight(18);
            rowBackground.setTranslateX(OUTER_PADDING);
            rowBackground.setTranslateY(rowY);
            rowBackground.setFill(Color.color(0.03, 0.03, 0.03, 0.72));
            rowBackground.setStroke(PlayerColorPalette.colorFor(playerId));
            rowBackground.setStrokeWidth(2);
            rowBackground.setEffect(dropShadow(Color.BLACK, 4));

            Text label = new Text("Player " + i);
            label.setFont(loadFont(26));
            label.setFill(PlayerColorPalette.colorFor(playerId));
            label.setEffect(dropShadow(Color.BLACK, 3));
            label.setTranslateX(OUTER_PADDING + 28);
            label.setTranslateY(rowY + 48);

            group.getChildren().addAll(rowBackground, label);

            Rectangle2D contentArea = new Rectangle2D(
                    OUTER_PADDING + ROW_LABEL_WIDTH,
                    rowY + 10,
                    rowWidth - ROW_LABEL_WIDTH - 24,
                    rowHeight - 20
            );

            addMelds(group, mockMelds.get(playerId), contentArea);
        }
    }

    private void addMelds(Group group, List<VisualMeld> melds, Rectangle2D area) {
        FullPlayAreaLayout layout = new FullPlayAreaLayout();
        List<MeldLayoutSlot> slots = layout.slots(melds, area);

        for (MeldLayoutSlot slot : slots) {
            Node cardView = cardViewFactory.createView(slot.card());
            cardView.setTranslateX(slot.position().getX());
            cardView.setTranslateY(slot.position().getY());
            cardView.setMouseTransparent(true);
            group.getChildren().add(cardView);
        }
    }

    private Button gameButton(String textValue, Color color) {
        Button button = new Button();

        Text text = new Text(textValue);
        text.setFont(loadFont(24));
        text.setStyle("-fx-fill: #ffffff;");

        button.setStyle(String.format("-fx-background-color: #%s ;", color.toString().substring(2, 8)));
        button.setPrefWidth(150);
        button.setPrefHeight(64);

        DropShadow textShadow = dropShadow(color.darker().darker(), 2);
        text.setEffect(textShadow);

        DropShadow buttonShadow = dropShadow(Color.color(0.1, 0.1, 0.1), 6);
        button.setEffect(buttonShadow);

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
