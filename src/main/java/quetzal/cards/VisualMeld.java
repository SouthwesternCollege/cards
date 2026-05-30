package quetzal.cards;

import java.util.List;

/**
 * Presentation-facing meld grouping.
 *
 * This is not the final domain Meld aggregate. It exists so the FXGL layer can
 * keep cards visually grouped by the player who originally created the meld.
 */
public record VisualMeld(
        PlayerId createdBy,
        List<Card> cards
) {

    public VisualMeld {
        if (createdBy == null) {
            throw new IllegalArgumentException("Created-by player cannot be null.");
        }

        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("A visual meld must contain at least one card.");
        }

        cards = List.copyOf(cards);
    }
}
