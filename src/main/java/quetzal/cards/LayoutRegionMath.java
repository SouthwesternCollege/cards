package quetzal.cards;

import javafx.geometry.Rectangle2D;

/**
 * Small layout helpers for deriving content regions from larger scene areas.
 */
public final class LayoutRegionMath {

    public static final double DEFAULT_VERTICAL_CONTENT_RATIO = 0.80;

    private LayoutRegionMath() {
    }

    public static Rectangle2D middleVerticalBand(Rectangle2D area) {
        return middleVerticalBand(area, DEFAULT_VERTICAL_CONTENT_RATIO);
    }

    public static Rectangle2D middleVerticalBand(Rectangle2D area, double contentRatio) {
        if (contentRatio <= 0 || contentRatio > 1) {
            throw new IllegalArgumentException("Content ratio must be in the interval (0, 1].");
        }

        double contentHeight = area.getHeight() * contentRatio;
        double y = area.getMinY() + (area.getHeight() - contentHeight) / 2.0;

        return new Rectangle2D(area.getMinX(), y, area.getWidth(), contentHeight);
    }

    public static double centeredCardY(Rectangle2D area) {
        Rectangle2D contentBand = middleVerticalBand(area);
        return contentBand.getMinY() + (contentBand.getHeight() - CardViewMetrics.renderedHeight()) / 2.0;
    }
}
