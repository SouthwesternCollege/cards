package quetzal.cards;

import java.util.List;

public record PlayerStateSnapshot(
        int playerId,
        String displayName,
        List<CardSnapshot> hand,
        int cumulativeScore,
        int castigosRemaining,
        boolean opened,
        List<Integer> customOrderCardIds
) {

    public PlayerStateSnapshot {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Player id must be positive.");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }

        if (hand == null) {
            throw new IllegalArgumentException("Hand cannot be null.");
        }

        if (castigosRemaining < 0) {
            throw new IllegalArgumentException("Castigos remaining cannot be negative.");
        }

        if (customOrderCardIds == null) {
            throw new IllegalArgumentException("Custom order card ids cannot be null.");
        }

        hand = List.copyOf(hand);
        customOrderCardIds = List.copyOf(customOrderCardIds);
    }
}
