package quetzal.cards;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Presentation layout calculator for a hand of cards.
 *
 * This is a small deep-module boundary: callers ask for card slots, while the
 * spacing/centering/compression details remain hidden here.
 */
public final class HandLayout {

    public static final double SELECTED_CARD_Y_OFFSET = -50.0;

    private static final double MAX_CARD_SPACING = 80.0;

    private final Rectangle2D handArea;

    public HandLayout(Rectangle2D handArea) {
        this.handArea = handArea;
    }

    public Rectangle2D handArea() {
        return handArea;
    }

    public double cardSpacing(int cardCount) {
        if (cardCount <= 1) {
            return MAX_CARD_SPACING;
        }

        double availableWidth = handArea.getWidth() - CardViewMetrics.renderedWidth();
        double idealSpacing = availableWidth / (cardCount - 1);

        return Math.max(CardViewMetrics.minVisibleCardSpacing(), Math.min(MAX_CARD_SPACING, idealSpacing));
    }

    public Point2D basePosition(int index, int cardCount) {
        if (cardCount == 0) {
            return new Point2D(handArea.getMinX(), verticallyCenteredY());
        }

        double spacing = cardSpacing(cardCount);
        double totalHandWidth = CardViewMetrics.renderedWidth() + spacing * (cardCount - 1);
        double startX = handArea.getMinX() + (handArea.getWidth() - totalHandWidth) / 2.0;
        return new Point2D(startX + index * spacing, verticallyCenteredY());
    }

    private double verticallyCenteredY() {
        return LayoutRegionMath.centeredCardY(handArea);
    }

    public Point2D visualPosition(int index, List<Card> cards, Set<CardId> selectedCardIds) {
        Point2D position = basePosition(index, cards.size());
        Card card = cards.get(index);

        if (selectedCardIds.contains(card.id())) {
            double liftedY = position.getY() + SELECTED_CARD_Y_OFFSET;
            return new Point2D(position.getX(), Math.max(handArea.getMinY(), liftedY));
        }

        return position;
    }

    public List<CardLayoutSlot> slots(List<Card> cards, Set<CardId> selectedCardIds) {
        List<CardLayoutSlot> slots = new ArrayList<>();

        for (int i = 0; i < cards.size(); i++) {
            slots.add(new CardLayoutSlot(
                    cards.get(i),
                    i,
                    basePosition(i, cards.size()),
                    visualPosition(i, cards, selectedCardIds),
                    i
            ));
        }

        return slots;
    }
}
