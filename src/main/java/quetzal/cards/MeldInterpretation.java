package quetzal.cards;

import java.util.List;

public record MeldInterpretation(
        MeldType meldType,
        List<Card> normalizedCards,
        List<JokerAssignment> jokerAssignments
) {

    public MeldInterpretation {
        if (meldType == null) {
            throw new IllegalArgumentException("Meld type cannot be null.");
        }

        if (normalizedCards == null || normalizedCards.size() < 3) {
            throw new IllegalArgumentException("A meld interpretation must contain at least three cards.");
        }

        if (normalizedCards.stream().anyMatch(card -> card == null)) {
            throw new IllegalArgumentException("A meld interpretation cannot contain null cards.");
        }

        normalizedCards = List.copyOf(normalizedCards);
        jokerAssignments = List.copyOf(jokerAssignments == null ? List.of() : jokerAssignments);
    }
}
