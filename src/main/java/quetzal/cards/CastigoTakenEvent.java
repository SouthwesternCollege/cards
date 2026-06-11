package quetzal.cards;

import java.util.List;

/**
 * Event emitted when the active player takes castigo.
 */
public record CastigoTakenEvent(
        PlayerId playerId,
        Card discardCard,
        List<Card> drawnCards
) implements GameEvent {

    public CastigoTakenEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (discardCard == null) {
            throw new IllegalArgumentException("Discard card cannot be null.");
        }

        if (drawnCards == null) {
            throw new IllegalArgumentException("Drawn cards cannot be null.");
        }

        drawnCards = List.copyOf(drawnCards);
    }

    public List<Card> allCards() {
        java.util.ArrayList<Card> cards = new java.util.ArrayList<>();
        cards.add(discardCard);
        cards.addAll(drawnCards);
        return List.copyOf(cards);
    }
}
