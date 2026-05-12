package quetzal.cards;

import javafx.geometry.Rectangle2D;

public class GameLayout {

    private static final double HUD_WIDTH_RATIO = 0.20;

    private static final double OPPONENT_PLAYED_HEIGHT_RATIO = 0.30;
    private static final double PLAYER_PLAYED_HEIGHT_RATIO = 0.30;
    private static final double PLAYER_HAND_HEIGHT_RATIO = 0.30;
    private static final double BUTTON_AREA_HEIGHT_RATIO = 0.10;

    private final double sceneWidth;
    private final double sceneHeight;

    private final double hudWidth;
    private final double tableX;
    private final double tableWidth;

    public GameLayout(double sceneWidth, double sceneHeight) {
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;

        this.hudWidth = sceneWidth * HUD_WIDTH_RATIO;
        this.tableX = hudWidth;
        this.tableWidth = sceneWidth - hudWidth;
    }

    public Rectangle2D getHudArea() {
        return new Rectangle2D(0, 0, hudWidth, sceneHeight);
    }

    public Rectangle2D getOpponentPlayedArea() {
        return new Rectangle2D(
                tableX,
                0,
                tableWidth,
                sceneHeight * OPPONENT_PLAYED_HEIGHT_RATIO
        );
    }

    public Rectangle2D getPlayerPlayedArea() {
        double y = sceneHeight * OPPONENT_PLAYED_HEIGHT_RATIO;

        return new Rectangle2D(
                tableX,
                y,
                tableWidth,
                sceneHeight * PLAYER_PLAYED_HEIGHT_RATIO
        );
    }

    public Rectangle2D getPlayerHandArea() {
        double y = sceneHeight * (OPPONENT_PLAYED_HEIGHT_RATIO + PLAYER_PLAYED_HEIGHT_RATIO);

        return new Rectangle2D(
                tableX,
                y,
                tableWidth,
                sceneHeight * PLAYER_HAND_HEIGHT_RATIO
        );
    }

    public Rectangle2D getButtonArea() {
        double y = sceneHeight * (
                OPPONENT_PLAYED_HEIGHT_RATIO
                        + PLAYER_PLAYED_HEIGHT_RATIO
                        + PLAYER_HAND_HEIGHT_RATIO
        );

        return new Rectangle2D(
                tableX,
                y,
                tableWidth,
                sceneHeight * BUTTON_AREA_HEIGHT_RATIO
        );
    }

    public double getSceneWidth() {
        return sceneWidth;
    }

    public double getSceneHeight() {
        return sceneHeight;
    }

    public double getHudWidth() {
        return hudWidth;
    }

    public double getTableX() {
        return tableX;
    }

    public double getTableWidth() {
        return tableWidth;
    }
}