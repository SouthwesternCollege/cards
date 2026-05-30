package quetzal.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Presentation-layer storage for visual meld groups.
 *
 * This keeps Milestone 4C from pretending that the full domain GameState exists
 * yet. Later, this should be fed by PlayArea/GameState rather than Hand.
 */
public final class VisualMeldStore {

    private final List<VisualMeld> melds = new ArrayList<>();

    public void add(VisualMeld meld) {
        if (meld == null) {
            throw new IllegalArgumentException("Visual meld cannot be null.");
        }

        melds.add(meld);
    }

    public List<VisualMeld> all() {
        return Collections.unmodifiableList(melds);
    }

    public List<VisualMeld> meldsFor(PlayerId playerId) {
        return melds.stream()
                .filter(meld -> meld.createdBy().equals(playerId))
                .toList();
    }
}
