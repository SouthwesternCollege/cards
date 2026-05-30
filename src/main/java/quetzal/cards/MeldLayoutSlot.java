package quetzal.cards;

import javafx.geometry.Point2D;

public record MeldLayoutSlot(
        VisualMeld meld,
        Card card,
        int meldIndex,
        int cardIndex,
        Point2D position,
        int zIndex
) {
}
