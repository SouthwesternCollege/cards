package quetzal.cards;

import javafx.scene.paint.Color;

public final class PlayerColorPalette {

    private PlayerColorPalette() {
    }

    public static Color colorFor(PlayerId playerId) {
        return switch (playerId.value()) {
            case 1 -> Color.DODGERBLUE;
            case 2 -> Color.RED;
            case 3 -> Color.GOLD;
            case 4 -> Color.LIMEGREEN;
            default -> Color.WHITE;
        };
    }
}
