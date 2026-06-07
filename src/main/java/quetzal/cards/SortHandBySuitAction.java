package quetzal.cards;

/**
 * Request to sort a player's current hand by suit.
 *
 * This changes current hand order, not saved custom hand order.
 */
public record SortHandBySuitAction(PlayerId playerId) implements GameAction {

    public SortHandBySuitAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
