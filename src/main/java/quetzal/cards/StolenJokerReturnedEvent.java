package quetzal.cards;

/**
 * Event emitted when a closed player must return a stolen joker because they did
 * not use it while opening during the same turn.
 */
public record StolenJokerReturnedEvent(
        PlayerId playerId,
        MeldState meld,
        Card returnedJoker,
        Card restoredReplacementCard
) implements GameEvent {

    public StolenJokerReturnedEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (meld == null) {
            throw new IllegalArgumentException("Meld cannot be null.");
        }

        if (returnedJoker == null || !returnedJoker.isJoker()) {
            throw new IllegalArgumentException("Returned card must be a joker.");
        }

        if (restoredReplacementCard == null || restoredReplacementCard.isJoker()) {
            throw new IllegalArgumentException("Restored card must be natural.");
        }
    }
}
