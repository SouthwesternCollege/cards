package quetzal.cards;

public record StolenJokerObligation(
        PlayerId playerId,
        CardId jokerId,
        MeldId sourceMeldId
) {

    public StolenJokerObligation {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (jokerId == null) {
            throw new IllegalArgumentException("Joker id cannot be null.");
        }

        if (sourceMeldId == null) {
            throw new IllegalArgumentException("Source meld id cannot be null.");
        }
    }
}
