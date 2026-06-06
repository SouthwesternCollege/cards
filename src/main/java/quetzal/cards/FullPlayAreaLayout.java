package quetzal.cards;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-table layout for read-only player rows.
 *
 * This intentionally uses tighter vertical row overlap than the normal
 * interactive MeldLayout because the full-table view is an overview/debug view.
 */
public final class FullPlayAreaLayout {

    private static final double PREFERRED_CARD_SPACING_RATIO = 0.24;
    private static final double PREFERRED_MELD_GAP = 70.0;
    private static final double MIN_MELD_GAP = 40.0;

    /**
     * Full view rows overlap more aggressively than normal play-area rows.
     * Normal MeldLayout currently uses 0.72.
     */
    private static final double ROW_VERTICAL_STEP_RATIO = 0.25;

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
        double preferredCardSpacing = preferredCardSpacing();
        double minimumCardSpacing = CardViewMetrics.minVisibleCardSpacing();

        if (totalWidth(melds, preferredCardSpacing, PREFERRED_MELD_GAP) <= area.getWidth()) {
            return new LayoutMetrics(preferredCardSpacing, PREFERRED_MELD_GAP);
        }

        if (totalWidth(melds, minimumCardSpacing, MIN_MELD_GAP) <= area.getWidth()) {
            return new LayoutMetrics(minimumCardSpacing, MIN_MELD_GAP);
        }

        return new LayoutMetrics(minimumCardSpacing, MIN_MELD_GAP);
    }

    private double preferredCardSpacing() {
        return CardViewMetrics.renderedWidth() * PREFERRED_CARD_SPACING_RATIO;
    }

    private double rowVerticalStep() {
        return CardViewMetrics.renderedHeight() * ROW_VERTICAL_STEP_RATIO;
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

        double rowStep = rowVerticalStep();
        double totalHeight = CardViewMetrics.renderedHeight() + rowStep * Math.max(0, rows.size() - 1);
        Rectangle2D contentBand = LayoutRegionMath.middleVerticalBand(area);
        double startY = contentBand.getMinY() + (contentBand.getHeight() - totalHeight) / 2.0;
        int zIndex = 0;
        int globalMeldIndex = 0;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = rows.get(rowIndex);
            double x = area.getMinX() + (area.getWidth() - row.width()) / 2.0;
            double y = startY + rowIndex * rowStep;

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
        return CardViewMetrics.renderedWidth() + cardSpacing * Math.max(0, cardCount - 1);
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
