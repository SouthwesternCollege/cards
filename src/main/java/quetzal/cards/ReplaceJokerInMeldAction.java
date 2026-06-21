package quetzal.cards;

/**
 * Request for the active player to replace a joker in an existing meld with a
 * natural card from their hand, taking the joker into their hand.
 */
public record ReplaceJokerInMeldAction(
        PlayerId playerId,
        MeldId meldId,
        CardId replacementCardId
) implements GameAction {

    public ReplaceJokerInMeldAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (meldId == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (replacementCardId == null) {
            throw new IllegalArgumentException("Replacement card id cannot be null.");
        }
    }
}
