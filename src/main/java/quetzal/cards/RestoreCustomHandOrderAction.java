package quetzal.cards;

/**
 * Request to restore the player's current hand order to their saved custom order.
 */
public record RestoreCustomHandOrderAction(PlayerId playerId) implements GameAction {

    public RestoreCustomHandOrderAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
