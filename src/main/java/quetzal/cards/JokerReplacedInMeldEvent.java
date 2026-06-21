package quetzal.cards;

/**
 * Event emitted when a player replaces a joker in an existing meld.
 *
 * The meld remains owned by its original creator. The event records who made
 * the replacement, which natural card entered the meld, and which joker was
 * returned to the replacing player's hand.
 */
public record JokerReplacedInMeldEvent(
        PlayerId replacedBy,
        MeldState meld,
        Card replacementCard,
        Card returnedJoker
) implements GameEvent {

    public JokerReplacedInMeldEvent {
        if (replacedBy == null) {
            throw new IllegalArgumentException("Replaced-by player cannot be null.");
        }

        if (meld == null) {
            throw new IllegalArgumentException("Meld cannot be null.");
        }

        if (replacementCard == null) {
            throw new IllegalArgumentException("Replacement card cannot be null.");
        }

        if (returnedJoker == null || !returnedJoker.isJoker()) {
            throw new IllegalArgumentException("Returned card must be a joker.");
        }
    }
}
