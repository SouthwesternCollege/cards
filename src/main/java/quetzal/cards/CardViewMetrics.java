package quetzal.cards;

import javafx.scene.image.Image;

/**
 * Shared presentation metrics for rendered cards.
 *
 * The source card size is derived from the standard deck sprite sheet contract:
 * 13 columns by 4 rows. The rendered size is the source cell size multiplied by
 * a configurable render scale.
 */
public final class CardViewMetrics {

    public static final int STANDARD_DECK_COLUMNS = 13;
    public static final int STANDARD_DECK_ROWS = 4;

    private static final double DEFAULT_SPRITE_WIDTH = 71.0;
    private static final double DEFAULT_SPRITE_HEIGHT = 95.0;
    private static final double DEFAULT_RENDER_SCALE = 2.0;

    private static double spriteWidth = DEFAULT_SPRITE_WIDTH;
    private static double spriteHeight = DEFAULT_SPRITE_HEIGHT;
    private static double renderScale = DEFAULT_RENDER_SCALE;

    private CardViewMetrics() {
    }

    public static void configureFromStandardDeckSheet(Image standardDeckSpriteSheet, double scale) {
        if (standardDeckSpriteSheet == null) {
            throw new IllegalArgumentException("Standard deck sprite sheet cannot be null.");
        }

        if (scale <= 0) {
            throw new IllegalArgumentException("Card render scale must be positive.");
        }

        spriteWidth = standardDeckSpriteSheet.getWidth() / STANDARD_DECK_COLUMNS;
        spriteHeight = standardDeckSpriteSheet.getHeight() / STANDARD_DECK_ROWS;
        renderScale = scale;
    }

    public static double spriteWidth() {
        return spriteWidth;
    }

    public static double spriteHeight() {
        return spriteHeight;
    }

    public static double renderScale() {
        return renderScale;
    }

    public static double renderedWidth() {
        return spriteWidth * renderScale;
    }

    public static double renderedHeight() {
        return spriteHeight * renderScale;
    }

    public static double minVisibleCardSpacing() {
        return renderedWidth() * 0.20;
    }
}
