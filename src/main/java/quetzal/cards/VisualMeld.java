package quetzal.cards;

import java.util.List;

/**
 * Presentation-facing meld grouping.
 */
public record VisualMeld(
        MeldId id,
        PlayerId createdBy,
        List<Card> cards
) {

    public VisualMeld {
        if (id == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (createdBy == null) {
            throw new IllegalArgumentException("Created-by player cannot be null.");
        }

        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("A visual meld must contain at least one card.");
        }

        cards = List.copyOf(cards);
    }

    public VisualMeld(PlayerId createdBy, List<Card> cards) {
        this(new MeldId(1), createdBy, cards);
    }

    public VisualMeld withCards(List<Card> cards) {
        return new VisualMeld(id, createdBy, cards);
    }
}
