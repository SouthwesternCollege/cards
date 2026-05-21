package quetzal.cards;

import java.util.List;

public record MoveResult(
        boolean succeeded,
        List<String> messages,
        List<StolenJokerObligation> jokerObligations
) {

    public MoveResult {
        messages = List.copyOf(messages == null ? List.of() : messages);
        jokerObligations = List.copyOf(jokerObligations == null ? List.of() : jokerObligations);

        if (!succeeded && messages.isEmpty()) {
            throw new IllegalArgumentException("A failed move result must include at least one message.");
        }
    }

    public static MoveResult success() {
        return new MoveResult(true, List.of(), List.of());
    }

    public static MoveResult success(List<String> messages, List<StolenJokerObligation> jokerObligations) {
        return new MoveResult(true, messages, jokerObligations);
    }

    public static MoveResult failure(String message) {
        return new MoveResult(false, List.of(message), List.of());
    }

    public static MoveResult failure(List<String> messages) {
        return new MoveResult(false, messages, List.of());
    }
}
