package quetzal.cards;

/**
 * Event emitted when a card is drawn into a player's hand.
 */
public record CardDrawnEvent(PlayerId playerId, Card card) implements GameEvent {

    public CardDrawnEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }
    }
}
