package quetzal.cards;

import java.util.List;

public record AddToMeldMove(
        PlayerId playerId,
        MeldId meldId,
        List<CardId> cardIds,
        MeldPlacement placement
) implements Move {

    public AddToMeldMove {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (meldId == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("An add-to-meld move must include at least one card id.");
        }

        if (cardIds.stream().anyMatch(cardId -> cardId == null)) {
            throw new IllegalArgumentException("Card ids cannot contain null.");
        }

        if (placement == null) {
            throw new IllegalArgumentException("Meld placement cannot be null.");
        }

        cardIds = List.copyOf(cardIds);
    }

    @Override
    public MoveType type() {
        return MoveType.ADD_TO_MELD;
    }
}
