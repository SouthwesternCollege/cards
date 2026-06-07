package quetzal.cards;

import java.util.List;

/**
 * Result of asking GameController to apply an action.
 */
public final class ActionResult {

    private final boolean success;
    private final String message;
    private final List<GameEvent> events;

    private ActionResult(boolean success, String message, List<GameEvent> events) {
        this.success = success;
        this.message = message == null ? "" : message;
        this.events = List.copyOf(events == null ? List.of() : events);
    }

    public static ActionResult success(GameEvent... events) {
        return new ActionResult(true, "", List.of(events));
    }

    public static ActionResult failure(String message) {
        return new ActionResult(false, message, List.of());
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public List<GameEvent> events() {
        return events;
    }
}
