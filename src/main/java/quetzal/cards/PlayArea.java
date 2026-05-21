package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

public final class PlayArea {

    private final List<Meld> melds = new ArrayList<>();

    public List<Meld> melds() {
        return List.copyOf(melds);
    }

    public void addMeld(Meld meld) {
        if (meld == null) {
            throw new IllegalArgumentException("Meld cannot be null.");
        }

        if (contains(meld.id())) {
            throw new IllegalArgumentException("Play area already contains meld: " + meld.id());
        }

        melds.add(meld);
    }

    public Meld getMeld(MeldId meldId) {
        return melds.stream()
                .filter(meld -> meld.id().equals(meldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Meld is not in play area: " + meldId));
    }

    public boolean contains(MeldId meldId) {
        return melds.stream().anyMatch(meld -> meld.id().equals(meldId));
    }

    public Meld removeMeld(MeldId meldId) {
        Meld meld = getMeld(meldId);
        melds.remove(meld);
        return meld;
    }
}
