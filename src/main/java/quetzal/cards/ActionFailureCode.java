package quetzal.cards;

/**
 * Machine-readable reason an action failed.
 *
 * User-facing text still lives in ActionResult.message().
 */
public enum ActionFailureCode {
    NONE,
    NULL_ACTION,
    UNSUPPORTED_ACTION,
    NOT_ACTIVE_PLAYER,
    WRONG_TURN_PHASE,
    CARD_NOT_IN_HAND,
    INVALID_MELD,
    INVALID_HAND_ORDER,
    GENERAL_RULE_VIOLATION
}
