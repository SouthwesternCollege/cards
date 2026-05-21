package quetzal.cards;

public record StealJokerMove(
        PlayerId playerId,
        MeldId sourceMeldId,
        CardId jokerId,
        CardId replacementCardId
) implements Move {

    public StealJokerMove {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (sourceMeldId == null) {
            throw new IllegalArgumentException("Source meld id cannot be null.");
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
        return MoveType.STEAL_JOKER;
    }
}
