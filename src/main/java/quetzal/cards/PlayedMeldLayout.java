package quetzal.cards;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Presentation layout calculator for played melds.
 *
 * Milestone 4A.2 originally centered only one newly played meld, which made
 * later melds stack on top of the previous meld. This version treats each
 * played meld as a separate visual group and centers the whole row of melds
 * inside the player meld area. The later play-area layout milestone should
 * expand this into wrapping, compression, creator grouping, and carousel/full-table modes.
 */
public final class PlayedMeldLayout {

    private static final double CARD_SPACING = 30.0;
    private static final double MELD_GAP = 70.0;

    public List<CardLayoutSlot> centeredSlotsForMeld(List<Card> cards, Rectangle2D area) {
        List<List<Card>> melds = new ArrayList<>();
        melds.add(cards);
        return centeredSlotsForMelds(melds, area);
    }

    public List<CardLayoutSlot> centeredSlotsForMelds(List<List<Card>> melds, Rectangle2D area) {
        List<CardLayoutSlot> slots = new ArrayList<>();

        List<List<Card>> nonEmptyMelds = melds.stream()
                .filter(meld -> !meld.isEmpty())
                .toList();

        if (nonEmptyMelds.isEmpty()) {
            return slots;
        }

        double totalWidth = totalWidth(nonEmptyMelds);
        double x = area.getMinX() + (area.getWidth() - totalWidth) / 2.0;
        double y = area.getMinY() + (area.getHeight() - HandLayout.CARD_HEIGHT) / 2.0;

        int zIndex = 0;

        for (int meldIndex = 0; meldIndex < nonEmptyMelds.size(); meldIndex++) {
            List<Card> meld = nonEmptyMelds.get(meldIndex);

            for (int cardIndex = 0; cardIndex < meld.size(); cardIndex++) {
                Point2D position = new Point2D(x + cardIndex * CARD_SPACING, y);
                slots.add(new CardLayoutSlot(meld.get(cardIndex), cardIndex, position, position, zIndex++));
            }

            x += meldWidth(meld) + MELD_GAP;
        }

        return slots;
    }

    private double totalWidth(List<List<Card>> melds) {
        double width = 0;

        for (int i = 0; i < melds.size(); i++) {
            width += meldWidth(melds.get(i));

            if (i < melds.size() - 1) {
                width += MELD_GAP;
            }
        }

        return width;
    }

    private double meldWidth(List<Card> cards) {
        if (cards.isEmpty()) {
            return 0;
        }

        return HandLayout.CARD_WIDTH + CARD_SPACING * (cards.size() - 1);
    }
}
