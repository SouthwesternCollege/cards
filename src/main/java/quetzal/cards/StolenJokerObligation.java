package quetzal.cards;

/**
 * Temporary turn-level obligation created when a closed player replaces a joker.
 *
 * The player may keep the stolen joker only if they use it while opening during
 * the same turn. Otherwise the replacement must be reversed before the turn can
 * end.
 */
public record StolenJokerObligation(
        MeldId meldId,
        Card joker,
        Card replacementCard
) {

    public StolenJokerObligation {
        if (meldId == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (joker == null || !joker.isJoker()) {
            throw new IllegalArgumentException("Obligation card must be a joker.");
        }

        if (replacementCard == null || replacementCard.isJoker()) {
            throw new IllegalArgumentException("Replacement card must be natural.");
        }
    }
}
