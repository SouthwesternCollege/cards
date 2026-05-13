package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GameHUD {

    private static GameHUD activeHUD;

    private final Rectangle2D hudArea;
    private final Font labelFont;
    private final Font valueFont;

    private final Text handRankLabel;
    private final Text handRankValue;

    public GameHUD(GameLayout gameLayout) {
        this.hudArea = gameLayout.getHudArea();

        this.labelFont = Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 18);
        this.valueFont = Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 24);

        this.handRankLabel = new Text("Hand Rank");
        this.handRankValue = new Text("");

        initializeHandRankText();

        activeHUD = this;
    }

    private void initializeHandRankText() {
        double paddingX = 24;
        double startY = 80;

        handRankLabel.setFont(labelFont);
        handRankLabel.setFill(Color.color(0.75, 0.75, 0.75));

        handRankValue.setFont(valueFont);
        handRankValue.setFill(Color.WHITE);

        handRankLabel.setTranslateX(hudArea.getMinX() + paddingX);
        handRankLabel.setTranslateY(startY);

        handRankValue.setTranslateX(hudArea.getMinX() + paddingX);
        handRankValue.setTranslateY(startY + 36);

        FXGL.getGameScene().addUINode(handRankLabel);
        FXGL.getGameScene().addUINode(handRankValue);
    }

    public void setHandRank(String rank) {
        handRankValue.setText(rank);
    }

    public static void updateHandRank(String rank) {
        if (activeHUD != null) {
            activeHUD.setHandRank(rank);
        }
    }
}