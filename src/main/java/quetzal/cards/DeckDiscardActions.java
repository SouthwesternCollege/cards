package quetzal.cards;

import javafx.geometry.Point2D;

/**
 * Presentation callback interface for deck/discard UI actions.
 *
 * This is intentionally small and provisional. The real implementation should
 * eventually delegate to GameState / TurnController instead of directly to Hand.
 */
public interface DeckDiscardActions {

    void drawFromDeck(Point2D sourcePosition);

    void takeCastigo(Point2D sourcePosition);

    void passCastigo();
}
