package quetzal.cards;

import java.util.List;

public record CreateMeldMove(
        PlayerId playerId,
        List<CardId> cardIds
) implements Move {

    public CreateMeldMove {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (cardIds == null || cardIds.size() < 3) {
            throw new IllegalArgumentException("A create-meld move must include at least three card ids.");
        }

        if (cardIds.stream().anyMatch(cardId -> cardId == null)) {
            throw new IllegalArgumentException("Card ids cannot contain null.");
        }

        cardIds = List.copyOf(cardIds);
    }

    @Override
    public MoveType type() {
        return MoveType.CREATE_MELD;
    }
}
