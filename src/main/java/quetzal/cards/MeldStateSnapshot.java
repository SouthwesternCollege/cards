package quetzal.cards;

import java.util.List;

public record MeldStateSnapshot(
        int createdByPlayerId,
        String meldType,
        List<CardSnapshot> cards
) {

    public MeldStateSnapshot {
        if (createdByPlayerId <= 0) {
            throw new IllegalArgumentException("Created-by player id must be positive.");
        }

        if (meldType == null || meldType.isBlank()) {
            throw new IllegalArgumentException("Meld type cannot be blank.");
        }

        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Meld cards cannot be empty.");
        }

        cards = List.copyOf(cards);
    }
}
