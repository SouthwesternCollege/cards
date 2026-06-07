package quetzal.cards;

/**
 * Event emitted when a player's current hand order is saved as custom order.
 */
public record CustomHandOrderSavedEvent(PlayerId playerId) implements GameEvent {

    public CustomHandOrderSavedEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
