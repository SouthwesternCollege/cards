package quetzal.cards;

/**
 * Event emitted when a player creates a rules-level meld.
 */
public record MeldCreatedEvent(PlayerId playerId, MeldState meld) implements GameEvent {

    public MeldCreatedEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (meld == null) {
            throw new IllegalArgumentException("Meld cannot be null.");
        }
    }
}
