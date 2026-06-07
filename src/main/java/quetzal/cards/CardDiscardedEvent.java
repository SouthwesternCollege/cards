package quetzal.cards;

/**
 * Event emitted when a card is discarded from a player's hand to the discard pile.
 */
public record CardDiscardedEvent(PlayerId playerId, Card card) implements GameEvent {

    public CardDiscardedEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }
    }
}
