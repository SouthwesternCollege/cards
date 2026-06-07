package quetzal.cards;

import java.util.List;

/**
 * Request for a player to create a new meld from cards in their hand.
 */
public record CreateMeldAction(PlayerId playerId, List<CardId> cardIds) implements GameAction {

    public CreateMeldAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("Card ids cannot be empty.");
        }

        cardIds = List.copyOf(cardIds);
    }
}
