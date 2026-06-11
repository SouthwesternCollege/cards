package quetzal.cards;

/**
 * Request for the active player to take the available castigo.
 *
 * Milestone 6C scope: active-player castigo only.
 */
public record TakeCastigoAction(PlayerId playerId) implements GameAction {

    public TakeCastigoAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
