package quetzal.cards;

/**
 * Request for a player to draw one card from the deck.
 */
public record DrawFromDeckAction(PlayerId playerId) implements GameAction {

    public DrawFromDeckAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }
    }
}
