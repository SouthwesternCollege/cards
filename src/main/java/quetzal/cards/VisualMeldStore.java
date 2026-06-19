package quetzal.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Presentation-layer storage for visual meld groups.
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

    public Optional<VisualMeld> find(MeldId meldId) {
        return melds.stream()
                .filter(meld -> meld.id().equals(meldId))
                .findFirst();
    }

    public List<VisualMeld> meldsFor(PlayerId playerId) {
        return melds.stream()
                .filter(meld -> meld.createdBy().equals(playerId))
                .toList();
    }

    public void replace(VisualMeld updatedMeld) {
        for (int i = 0; i < melds.size(); i++) {
            if (melds.get(i).id().equals(updatedMeld.id())) {
                melds.set(i, updatedMeld);
                return;
            }
        }

        melds.add(updatedMeld);
    }

    public void clear() {
        melds.clear();
    }
}
