package quetzal.cards;

/**
 * Rules-level phase of the current turn.
 *
 * Milestone 5A only establishes the vocabulary. Later milestones will enforce
 * phase-specific legal actions.
 */
public enum TurnPhase {
    DRAW_OR_CASTIGO,
    MELD,
    DISCARD,
    ROUND_OVER
}
