package quetzal.cards;

/**
 * Request for the active player to add one hand card to an existing meld.
 */
public record AddCardToMeldAction(
        PlayerId playerId,
        MeldId meldId,
        CardId cardId
) implements GameAction {

    public AddCardToMeldAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (meldId == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (cardId == null) {
            throw new IllegalArgumentException("Card id cannot be null.");
        }
    }
}
