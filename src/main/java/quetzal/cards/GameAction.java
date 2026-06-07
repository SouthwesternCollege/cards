package quetzal.cards;

/**
 * Marker interface for a rules-level request to change GameState.
 *
 * UI code should submit actions to GameController instead of mutating domain
 * state directly.
 */
public interface GameAction {
}
