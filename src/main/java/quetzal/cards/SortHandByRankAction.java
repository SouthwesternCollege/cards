package quetzal.cards;

/**
 * Request to sort a player's current hand by rank.
 *
 * This changes current hand order, not saved custom hand order.
 */
public record SortHandByRankAction(PlayerId playerId) implements GameAction {

    public SortHandByRankAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
