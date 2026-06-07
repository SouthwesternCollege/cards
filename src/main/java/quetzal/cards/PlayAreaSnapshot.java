package quetzal.cards;

import java.util.List;

public record PlayAreaSnapshot(List<MeldStateSnapshot> melds) {

    public PlayAreaSnapshot {
        if (melds == null) {
            throw new IllegalArgumentException("Melds cannot be null.");
        }

        melds = List.copyOf(melds);
    }
}
