package quetzal.cards;

import java.util.List;

public record DeckSnapshot(List<CardSnapshot> cards) {

    public DeckSnapshot {
        if (cards == null) {
            throw new IllegalArgumentException("Deck cards cannot be null.");
        }

        cards = List.copyOf(cards);
    }
}
