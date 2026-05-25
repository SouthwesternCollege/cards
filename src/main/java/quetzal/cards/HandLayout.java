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

    public static final double CARD_WIDTH = 142.0;
    public static final double CARD_HEIGHT = 190.0;
    public static final double CARD_TOP_PADDING = 60.0;
    public static final double SELECTED_CARD_Y_OFFSET = -50.0;

    private static final double MAX_CARD_SPACING = 80.0;
    private static final double MIN_VISIBLE_SPACING = 15.0;

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

        double availableWidth = handArea.getWidth() - CARD_WIDTH;
        double idealSpacing = availableWidth / (cardCount - 1);

        return Math.max(MIN_VISIBLE_SPACING, Math.min(MAX_CARD_SPACING, idealSpacing));
    }

    public Point2D basePosition(int index, int cardCount) {
        if (cardCount == 0) {
            return new Point2D(handArea.getMinX(), handArea.getMinY() + CARD_TOP_PADDING);
        }

        double spacing = cardSpacing(cardCount);
        double totalHandWidth = CARD_WIDTH + spacing * (cardCount - 1);
        double startX = handArea.getMinX() + (handArea.getWidth() - totalHandWidth) / 2.0;
        double y = handArea.getMinY() + CARD_TOP_PADDING;

        return new Point2D(startX + index * spacing, y);
    }

    public Point2D visualPosition(int index, List<Card> cards, Set<CardId> selectedCardIds) {
        Point2D position = basePosition(index, cards.size());
        Card card = cards.get(index);

        if (selectedCardIds.contains(card.id())) {
            return position.add(0, SELECTED_CARD_Y_OFFSET);
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
