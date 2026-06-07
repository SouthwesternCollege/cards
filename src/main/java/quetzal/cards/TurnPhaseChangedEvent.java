package quetzal.cards;

/**
 * Event emitted when the current turn phase changes.
 */
public record TurnPhaseChangedEvent(TurnPhase previousPhase, TurnPhase newPhase) implements GameEvent {

    public TurnPhaseChangedEvent {
        if (previousPhase == null) {
            throw new IllegalArgumentException("Previous phase cannot be null.");
        }

        if (newPhase == null) {
            throw new IllegalArgumentException("New phase cannot be null.");
        }
    }
}
