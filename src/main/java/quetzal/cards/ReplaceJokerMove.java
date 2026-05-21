package quetzal.cards;

public record ReplaceJokerMove(
        PlayerId playerId,
        MeldId meldId,
        CardId jokerId,
        CardId replacementCardId
) implements Move {

    public ReplaceJokerMove {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (meldId == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (jokerId == null) {
            throw new IllegalArgumentException("Joker id cannot be null.");
        }

        if (replacementCardId == null) {
            throw new IllegalArgumentException("Replacement card id cannot be null.");
        }
    }

    @Override
    public MoveType type() {
        return MoveType.REPLACE_JOKER;
    }
}
