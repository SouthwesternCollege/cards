package quetzal.cards;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Layout calculator for played melds.
 *
 * Current strategy:
 * 1. Preserve meld creation order left-to-right.
 * 2. Compress card overlap and meld gaps before wrapping.
 * 3. Wrap to multiple centered rows before shrinking cards.
 * 4. Keep normal card scale for now; scaling comes later if needed.
 */
public final class MeldLayout {

    private static final double PREFERRED_CARD_SPACING = 30.0;
    private static final double MIN_CARD_SPACING = 15.0;
    private static final double PREFERRED_MELD_GAP = 70.0;
    private static final double MIN_MELD_GAP = 40.0;
    private static final double ROW_VERTICAL_STEP = HandLayout.CARD_HEIGHT * 0.72;

    public List<MeldLayoutSlot> slots(List<VisualMeld> melds, Rectangle2D area) {
        List<VisualMeld> nonEmptyMelds = melds.stream()
                .filter(meld -> !meld.cards().isEmpty())
                .toList();

        if (nonEmptyMelds.isEmpty()) {
            return List.of();
        }

        LayoutMetrics metrics = chooseMetrics(nonEmptyMelds, area);
        List<Row> rows = buildRows(nonEmptyMelds, area, metrics);

        return buildSlots(rows, area, metrics);
    }

    private LayoutMetrics chooseMetrics(List<VisualMeld> melds, Rectangle2D area) {
        if (totalWidth(melds, PREFERRED_CARD_SPACING, PREFERRED_MELD_GAP) <= area.getWidth()) {
            return new LayoutMetrics(PREFERRED_CARD_SPACING, PREFERRED_MELD_GAP);
        }

        if (totalWidth(melds, MIN_CARD_SPACING, MIN_MELD_GAP) <= area.getWidth()) {
            return new LayoutMetrics(MIN_CARD_SPACING, MIN_MELD_GAP);
        }

        // Wrapping uses the minimum spacing before we eventually introduce scaling.
        return new LayoutMetrics(MIN_CARD_SPACING, MIN_MELD_GAP);
    }

    private List<Row> buildRows(List<VisualMeld> melds, Rectangle2D area, LayoutMetrics metrics) {
        List<Row> rows = new ArrayList<>();
        Row currentRow = new Row();

        for (VisualMeld meld : melds) {
            double meldWidth = meldWidth(meld, metrics.cardSpacing());

            if (!currentRow.isEmpty() && currentRow.widthWith(meldWidth, metrics.meldGap()) > area.getWidth()) {
                rows.add(currentRow);
                currentRow = new Row();
            }

            currentRow.add(meld, meldWidth, metrics.meldGap());
        }

        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        return rows;
    }

    private List<MeldLayoutSlot> buildSlots(List<Row> rows, Rectangle2D area, LayoutMetrics metrics) {
        List<MeldLayoutSlot> slots = new ArrayList<>();

        double totalHeight = HandLayout.CARD_HEIGHT + ROW_VERTICAL_STEP * Math.max(0, rows.size() - 1);
        double startY = area.getMinY() + (area.getHeight() - totalHeight) / 2.0;
        int zIndex = 0;
        int globalMeldIndex = 0;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = rows.get(rowIndex);
            double x = area.getMinX() + (area.getWidth() - row.width()) / 2.0;
            double y = startY + rowIndex * ROW_VERTICAL_STEP;

            for (VisualMeld meld : row.melds()) {
                for (int cardIndex = 0; cardIndex < meld.cards().size(); cardIndex++) {
                    Point2D position = new Point2D(x + cardIndex * metrics.cardSpacing(), y);
                    slots.add(new MeldLayoutSlot(meld, meld.cards().get(cardIndex), globalMeldIndex, cardIndex, position, zIndex++));
                }

                x += meldWidth(meld, metrics.cardSpacing()) + metrics.meldGap();
                globalMeldIndex++;
            }
        }

        return slots;
    }

    private double totalWidth(List<VisualMeld> melds, double cardSpacing, double meldGap) {
        double width = 0;

        for (int i = 0; i < melds.size(); i++) {
            width += meldWidth(melds.get(i), cardSpacing);

            if (i < melds.size() - 1) {
                width += meldGap;
            }
        }

        return width;
    }

    private double meldWidth(VisualMeld meld, double cardSpacing) {
        int cardCount = meld.cards().size();
        return HandLayout.CARD_WIDTH + cardSpacing * Math.max(0, cardCount - 1);
    }

    private record LayoutMetrics(double cardSpacing, double meldGap) {
    }

    private static final class Row {
        private final List<VisualMeld> melds = new ArrayList<>();
        private double width = 0;

        void add(VisualMeld meld, double meldWidth, double meldGap) {
            if (!melds.isEmpty()) {
                width += meldGap;
            }

            melds.add(meld);
            width += meldWidth;
        }

        double widthWith(double meldWidth, double meldGap) {
            if (melds.isEmpty()) {
                return meldWidth;
            }

            return width + meldGap + meldWidth;
        }

        boolean isEmpty() {
            return melds.isEmpty();
        }

        double width() {
            return width;
        }

        List<VisualMeld> melds() {
            return melds;
        }
    }
}
