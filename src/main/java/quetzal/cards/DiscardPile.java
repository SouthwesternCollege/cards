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
    private PlayerId topDiscardedBy;

    public void add(Card card) {
        add(card, null);
    }

    public void add(Card card, PlayerId discardedBy) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }

        cards.add(card);
        topDiscardedBy = discardedBy;
    }

    public Optional<Card> topCard() {
        if (cards.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(cards.get(cards.size() - 1));
    }

    public Optional<PlayerId> topDiscardedBy() {
        return Optional.ofNullable(topDiscardedBy);
    }

    public Card removeTopCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot remove from an empty discard pile.");
        }

        Card removed = cards.remove(cards.size() - 1);

        if (cards.isEmpty()) {
            topDiscardedBy = null;
        }

        return removed;
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
