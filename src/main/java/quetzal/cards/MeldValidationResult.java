package quetzal.cards;

import java.util.List;

public record MeldValidationResult(
        boolean valid,
        MeldType meldType,
        int cardCount,
        List<JokerAssignment> jokerAssignments,
        List<String> errors
) {

    public MeldValidationResult {
        jokerAssignments = List.copyOf(jokerAssignments == null ? List.of() : jokerAssignments);
        errors = List.copyOf(errors == null ? List.of() : errors);

        if (cardCount < 0) {
            throw new IllegalArgumentException("Card count cannot be negative.");
        }

        if (valid && meldType == null) {
            throw new IllegalArgumentException("A valid meld result must include a meld type.");
        }

        if (valid && cardCount < 3) {
            throw new IllegalArgumentException("A valid meld result must contain at least three cards.");
        }

        if (valid && !errors.isEmpty()) {
            throw new IllegalArgumentException("A valid meld result cannot contain errors.");
        }

        if (!valid && errors.isEmpty()) {
            throw new IllegalArgumentException("An invalid meld result must contain at least one error.");
        }
    }

    public static MeldValidationResult valid(MeldType meldType, int cardCount) {
        return valid(meldType, cardCount, List.of());
    }

    public static MeldValidationResult valid(MeldType meldType, int cardCount, List<JokerAssignment> jokerAssignments) {
        return new MeldValidationResult(true, meldType, cardCount, jokerAssignments, List.of());
    }

    public static MeldValidationResult invalid(String error) {
        return invalid(List.of(error));
    }

    public static MeldValidationResult invalid(List<String> errors) {
        return new MeldValidationResult(false, null, 0, List.of(), errors);
    }
}
