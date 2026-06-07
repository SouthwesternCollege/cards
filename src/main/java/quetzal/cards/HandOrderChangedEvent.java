package quetzal.cards;

import java.util.List;

/**
 * Event emitted when a player's current hand order changes.
 */
public record HandOrderChangedEvent(PlayerId playerId, List<Card> orderedCards) implements GameEvent {

    public HandOrderChangedEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (orderedCards == null) {
            throw new IllegalArgumentException("Ordered cards cannot be null.");
        }

        orderedCards = List.copyOf(orderedCards);
    }
}
