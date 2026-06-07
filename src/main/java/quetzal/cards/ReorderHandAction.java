package quetzal.cards;

import java.util.List;

/**
 * Request to set a player's current hand order explicitly.
 */
public record ReorderHandAction(PlayerId playerId, List<CardId> orderedCardIds) implements GameAction {

    public ReorderHandAction {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (orderedCardIds == null) {
            throw new IllegalArgumentException("Ordered card ids cannot be null.");
        }

        orderedCardIds = List.copyOf(orderedCardIds);
    }
}
