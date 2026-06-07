package quetzal.cards;

import java.util.List;

public record DiscardPileSnapshot(List<CardSnapshot> cards) {

    public DiscardPileSnapshot {
        if (cards == null) {
            throw new IllegalArgumentException("Discard pile cards cannot be null.");
        }

        cards = List.copyOf(cards);
    }
}
