package quetzal.cards;

public record JokerAssignment(
        CardId jokerId,
        Rank assignedRank,
        Suit assignedSuit
) {

    public JokerAssignment {
        if (jokerId == null) {
            throw new IllegalArgumentException("Joker id cannot be null.");
        }

        if (assignedRank == null) {
            throw new IllegalArgumentException("Assigned rank cannot be null.");
        }
    }
}
