package quetzal.cards;

import java.util.List;
import java.util.Optional;

public record MeldValidationResult(
        boolean valid,
        MeldType meldType,
        int cardCount,
        List<Card> normalizedCards,
        List<JokerAssignment> jokerAssignments,
        List<MeldValidationError> errors
) {

    public MeldValidationResult {
        normalizedCards = List.copyOf(normalizedCards == null ? List.of() : normalizedCards);
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

        if (valid && normalizedCards.size() != cardCount) {
            throw new IllegalArgumentException("A valid meld result must contain normalized cards matching card count.");
        }

        if (valid && !errors.isEmpty()) {
            throw new IllegalArgumentException("A valid meld result cannot contain errors.");
        }

        if (!valid && errors.isEmpty()) {
            throw new IllegalArgumentException("An invalid meld result must contain at least one error.");
        }
    }

    public static MeldValidationResult valid(MeldType meldType, List<Card> normalizedCards) {
        return valid(meldType, normalizedCards, List.of());
    }

    public static MeldValidationResult valid(MeldType meldType, List<Card> normalizedCards, List<JokerAssignment> jokerAssignments) {
        if (normalizedCards == null) {
            throw new IllegalArgumentException("Normalized cards cannot be null.");
        }

        return new MeldValidationResult(true, meldType, normalizedCards.size(), normalizedCards, jokerAssignments, List.of());
    }

    public static MeldValidationResult invalid(MeldValidationError error) {
        return invalid(List.of(error));
    }

    public static MeldValidationResult invalid(List<MeldValidationError> errors) {
        return new MeldValidationResult(false, null, 0, List.of(), List.of(), errors);
    }

    public Optional<MeldValidationError> primaryError() {
        List<MeldValidationError> priority = List.of(
                MeldValidationError.TOO_FEW_CARDS,
                MeldValidationError.NULL_CARD,
                MeldValidationError.TOO_MANY_JOKERS,
                MeldValidationError.ALL_JOKERS,
                MeldValidationError.CONSECUTIVE_JOKERS,
                MeldValidationError.DUPLICATE_SEQUENCE_RANK,
                MeldValidationError.MIXED_SUITS,
                MeldValidationError.NO_CONSECUTIVE_SEQUENCE,
                MeldValidationError.MIXED_RANKS,
                MeldValidationError.NO_VALID_MELD_TYPE
        );

        return priority.stream()
                .filter(errors::contains)
                .findFirst()
                .or(() -> errors.stream().findFirst());
    }

    public String displayText() {
        if (valid) {
            return switch (meldType) {
                case KIND -> "Valid kind meld";
                case STRAIGHT_FLUSH -> "Valid straight flush";
            };
        }

        return primaryError()
                .map(MeldValidationError::message)
                .orElse("Invalid meld.");
    }
}
