package quetzal.cards;

import java.util.List;

/**
 * Result of asking GameController to apply an action.
 */
public final class ActionResult {

    private final boolean success;
    private final ActionFailureCode failureCode;
    private final String message;
    private final List<GameEvent> events;

    private ActionResult(boolean success, ActionFailureCode failureCode, String message, List<GameEvent> events) {
        this.success = success;
        this.failureCode = failureCode == null ? ActionFailureCode.GENERAL_RULE_VIOLATION : failureCode;
        this.message = message == null ? "" : message;
        this.events = List.copyOf(events == null ? List.of() : events);
    }

    public static ActionResult success(GameEvent... events) {
        return new ActionResult(true, ActionFailureCode.NONE, "", List.of(events));
    }

    public static ActionResult failure(String message) {
        return failure(ActionFailureCode.GENERAL_RULE_VIOLATION, message);
    }

    public static ActionResult failure(ActionFailureCode failureCode, String message) {
        if (failureCode == ActionFailureCode.NONE) {
            throw new IllegalArgumentException("Failure code cannot be NONE for failed result.");
        }

        return new ActionResult(false, failureCode, message, List.of());
    }

    public boolean success() {
        return success;
    }

    public ActionFailureCode failureCode() {
        return failureCode;
    }

    public String message() {
        return message;
    }

    public List<GameEvent> events() {
        return events;
    }
}
