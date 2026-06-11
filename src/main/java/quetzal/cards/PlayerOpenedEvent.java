package quetzal.cards;

/**
 * Event emitted when a closed player satisfies the round opening requirement.
 */
public record PlayerOpenedEvent(PlayerId playerId) implements GameEvent {

    public PlayerOpenedEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
