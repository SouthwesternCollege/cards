package quetzal.cards;

/**
 * Request to save the player's current hand order as their custom order.
 */
public record SaveCustomHandOrderAction(PlayerId playerId) implements GameAction {

    public SaveCustomHandOrderAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
