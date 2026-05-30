package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GameHUD {

    private final Rectangle2D hudArea;
    private final Font titleFont;
    private final Font labelFont;
    private final Font valueFont;
    private final Font smallFont;

    private final List<PlayerHudRow> playerRows = new ArrayList<>();

    private final Text selectedMeldLabel;
    private final Text selectedMeldValue;

    public GameHUD(GameLayout gameLayout, PlayerHudModel playerHudModel) {
        if (playerHudModel == null) {
            throw new IllegalArgumentException("Player HUD model cannot be null.");
        }

        this.hudArea = gameLayout.getHudArea();

        this.titleFont = loadFont(32);
        this.labelFont = loadFont(18);
        this.valueFont = loadFont(22);
        this.smallFont = loadFont(14);

        this.selectedMeldLabel = new Text("Selected Meld");
        this.selectedMeldValue = new Text("");

        initializeTitle();
        initializePlayerRows(playerHudModel.playerStates());
        initializeSelectedMeldText();

    }

    private Font loadFont(double size) {
        return Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), size);
    }

    private void initializeTitle() {
        Text title = new Text("LA KIKA");
        title.setFont(titleFont);
        title.setFill(Color.WHITE);
        title.setEffect(dropShadow(Color.color(0.05, 0.15, 0.05), 4));
        title.setTranslateX(hudArea.getMinX() + 24);
        title.setTranslateY(hudArea.getMinY() + 48);

        FXGL.getGameScene().addUINode(title);
    }

    private void initializePlayerRows(List<PlayerHudState> players) {
        double rowX = hudArea.getMinX() + 18;
        double rowY = hudArea.getMinY() + 86;
        double rowWidth = hudArea.getWidth() - 36;
        double rowHeight = 118;
        double rowGap = 14;

        for (int i = 0; i < players.size(); i++) {
            PlayerHudRow row = new PlayerHudRow(
                    players.get(i),
                    rowX,
                    rowY + i * (rowHeight + rowGap),
                    rowWidth,
                    rowHeight
            );

            playerRows.add(row);
            FXGL.getGameScene().addUINode(row.root());
        }
    }

    private void initializeSelectedMeldText() {
        double paddingX = 24;
        double y = hudArea.getMaxY() - 120;

        selectedMeldLabel.setFont(labelFont);
        selectedMeldLabel.setFill(Color.color(0.75, 0.75, 0.75));
        selectedMeldLabel.setEffect(dropShadow(Color.BLACK, 2));

        selectedMeldValue.setFont(valueFont);
        selectedMeldValue.setFill(Color.WHITE);
        selectedMeldValue.setEffect(dropShadow(Color.BLACK, 2));

        selectedMeldLabel.setTranslateX(hudArea.getMinX() + paddingX);
        selectedMeldLabel.setTranslateY(y);

        selectedMeldValue.setTranslateX(hudArea.getMinX() + paddingX);
        selectedMeldValue.setTranslateY(y + 38);

        FXGL.getGameScene().addUINode(selectedMeldLabel);
        FXGL.getGameScene().addUINode(selectedMeldValue);
    }

    public void setSelectedMeld(String selectedMeld) {
        selectedMeldValue.setText(selectedMeld == null ? "" : selectedMeld);
    }

    public void setPlayerStates(List<PlayerHudState> playerStates) {
        int count = Math.min(playerRows.size(), playerStates.size());

        for (int i = 0; i < count; i++) {
            playerRows.get(i).update(playerStates.get(i));
        }
    }

    private DropShadow dropShadow(Color color, double offsetY) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(1);
        shadow.setOffsetY(offsetY);
        shadow.setColor(color);
        return shadow;
    }

    private class PlayerHudRow {

        private final Group root = new Group();
        private final Rectangle background;
        private final Text turnArrow;
        private final Text nameText;
        private final Text scoreText;
        private final Text cardsText;
        private final Text castigoText;
        private final Circle dealerChip;
        private final Text dealerText;

        PlayerHudRow(PlayerHudState state, double x, double y, double width, double height) {
            background = new Rectangle(width, height);
            background.setArcWidth(14);
            background.setArcHeight(14);
            background.setFill(Color.color(0.03, 0.03, 0.03, 0.78));
            background.setStroke(Color.color(1, 1, 1, 0.15));
            background.setEffect(dropShadow(Color.BLACK, 4));

            turnArrow = new Text("➜");
            turnArrow.setFont(valueFont);
            turnArrow.setEffect(dropShadow(Color.BLACK, 2));

            nameText = new Text();
            nameText.setFont(valueFont);
            nameText.setEffect(dropShadow(Color.BLACK, 2));

            scoreText = new Text();
            scoreText.setFont(smallFont);
            scoreText.setFill(Color.WHITE);
            scoreText.setEffect(dropShadow(Color.BLACK, 2));

            cardsText = new Text();
            cardsText.setFont(smallFont);
            cardsText.setFill(Color.WHITE);
            cardsText.setEffect(dropShadow(Color.BLACK, 2));

            castigoText = new Text();
            castigoText.setFont(smallFont);
            castigoText.setFill(Color.WHITE);
            castigoText.setEffect(dropShadow(Color.BLACK, 2));

            dealerChip = new Circle(13);
            dealerChip.setFill(Color.GOLD);
            dealerChip.setStroke(Color.color(0.2, 0.12, 0.0));
            dealerChip.setEffect(dropShadow(Color.BLACK, 2));

            dealerText = new Text("D");
            dealerText.setFont(smallFont);
            dealerText.setFill(Color.BLACK);

            root.setTranslateX(x);
            root.setTranslateY(y);

            background.setTranslateX(0);
            background.setTranslateY(0);

            turnArrow.setTranslateX(16);
            turnArrow.setTranslateY(36);

            nameText.setTranslateX(48);
            nameText.setTranslateY(36);

            dealerChip.setTranslateX(width - 32);
            dealerChip.setTranslateY(28);

            dealerText.setTranslateX(width - 38);
            dealerText.setTranslateY(33);

            scoreText.setTranslateX(28);
            scoreText.setTranslateY(64);

            cardsText.setTranslateX(28);
            cardsText.setTranslateY(84);

            castigoText.setTranslateX(28);
            castigoText.setTranslateY(104);

            root.getChildren().addAll(
                    background,
                    turnArrow,
                    nameText,
                    dealerChip,
                    dealerText,
                    scoreText,
                    cardsText,
                    castigoText
            );

            update(state);
        }

        Group root() {
            return root;
        }

        void update(PlayerHudState state) {
            Color playerColor = PlayerColorPalette.colorFor(state.playerId());
            Color nameColor = state.opened()
                    ? playerColor
                    : Color.color(0.48, 0.48, 0.48);

            if (state.activeTurn()) {
                background.setStroke(playerColor);
                background.setStrokeWidth(2.0);
                turnArrow.setVisible(true);
                turnArrow.setFill(playerColor);
            } else {
                background.setStroke(Color.color(1, 1, 1, 0.15));
                background.setStrokeWidth(1.0);
                turnArrow.setVisible(false);
            }

            nameText.setText(state.displayName());
            nameText.setFill(nameColor);

            scoreText.setText("Score: " + state.cumulativeScore());
            cardsText.setText("Cards: " + state.cardsRemaining());
            castigoText.setText("Castigos: " + state.castigosRemaining() + " remaining");

            dealerChip.setVisible(state.dealer());
            dealerText.setVisible(state.dealer());
        }
    }
}
