package quetzal.cards;

import javafx.geometry.Point2D;

public record CardLayoutSlot(
        Card card,
        int index,
        Point2D basePosition,
        Point2D visualPosition,
        int zIndex
) {
}
