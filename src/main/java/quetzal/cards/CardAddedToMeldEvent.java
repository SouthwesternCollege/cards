package quetzal.cards;

/**
 * Event emitted when a player adds a card to an existing rules-level meld.
 */
public record CardAddedToMeldEvent(
        PlayerId addedBy,
        MeldState meld,
        Card card
) implements GameEvent {

    public CardAddedToMeldEvent {
        if (addedBy == null) {
            throw new IllegalArgumentException("Added-by player cannot be null.");
        }

        if (meld == null) {
            throw new IllegalArgumentException("Meld cannot be null.");
        }

        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }
    }
}
