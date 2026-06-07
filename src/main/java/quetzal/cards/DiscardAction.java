package quetzal.cards;

/**
 * Request for a player to discard one card.
 */
public record DiscardAction(PlayerId playerId, CardId cardId) implements GameAction {

    public DiscardAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (cardId == null) {
            throw new IllegalArgumentException("Card id cannot be null.");
        }
    }
}
