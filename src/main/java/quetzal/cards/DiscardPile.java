package quetzal.cards;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Rules-level discard pile.
 *
 * The top discarded card is the only card currently relevant for castigo.
 */
public final class DiscardPile {

    private final List<Card> cards = new ArrayList<>();

    public void add(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }

        cards.add(card);
    }

    public Optional<Card> topCard() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(cards.get(cards.size() - 1));
    }

    public List<Card> cards() {
        return List.copyOf(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public DiscardPileSnapshot toSnapshot() {
        return new DiscardPileSnapshot(
                cards.stream()
                        .map(Card::toSnapshot)
                        .toList()
        );
    }

    public static DiscardPile fromSnapshot(DiscardPileSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Discard pile snapshot cannot be null.");
        }

        DiscardPile discardPile = new DiscardPile();

        for (CardSnapshot cardSnapshot : snapshot.cards()) {
            discardPile.add(Card.fromSnapshot(cardSnapshot));
        }

        return discardPile;
    }
}
