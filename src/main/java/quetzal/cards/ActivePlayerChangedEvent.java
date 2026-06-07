package quetzal.cards;

/**
 * Event emitted when the active player changes.
 */
public record ActivePlayerChangedEvent(PlayerId previousPlayerId, PlayerId newPlayerId) implements GameEvent {

    public ActivePlayerChangedEvent {
        if (previousPlayerId == null) {
            throw new IllegalArgumentException("Previous player id cannot be null.");
        }

        if (newPlayerId == null) {
            throw new IllegalArgumentException("New player id cannot be null.");
        }
    }
}
