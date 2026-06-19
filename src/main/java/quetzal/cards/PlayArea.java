package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Rules-level play area.
 */
public final class PlayArea {

    private final List<MeldState> melds = new ArrayList<>();
    private int nextMeldId = 1;

    public MeldId nextMeldId() {
        return new MeldId(nextMeldId++);
    }

    public void addMeld(MeldState meld) {
        if (meld == null) {
            throw new IllegalArgumentException("Meld cannot be null.");
        }

        melds.add(meld);
        nextMeldId = Math.max(nextMeldId, meld.id().value() + 1);
    }

    public MeldState meld(MeldId meldId) {
        for (MeldState meld : melds) {
            if (meld.id().equals(meldId)) {
                return meld;
            }
        }

        throw new IllegalArgumentException("Unknown meld id: " + meldId.value());
    }

    public void replaceMeld(MeldState updatedMeld) {
        if (updatedMeld == null) {
            throw new IllegalArgumentException("Updated meld cannot be null.");
        }

        for (int i = 0; i < melds.size(); i++) {
            if (melds.get(i).id().equals(updatedMeld.id())) {
                melds.set(i, updatedMeld);
                return;
            }
        }

        throw new IllegalArgumentException("Unknown meld id: " + updatedMeld.id().value());
    }

    public List<MeldState> melds() {
        return List.copyOf(melds);
    }

    public List<MeldState> meldsCreatedBy(PlayerId playerId) {
        return melds.stream()
                .filter(meld -> meld.createdBy().equals(playerId))
                .toList();
    }

    public PlayAreaSnapshot toSnapshot() {
        return new PlayAreaSnapshot(
                melds.stream()
                        .map(MeldState::toSnapshot)
                        .toList()
        );
    }

    public static PlayArea fromSnapshot(PlayAreaSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Play area snapshot cannot be null.");
        }

        PlayArea playArea = new PlayArea();

        for (MeldStateSnapshot meldSnapshot : snapshot.melds()) {
            playArea.addMeld(MeldState.fromSnapshot(meldSnapshot));
        }

        return playArea;
    }
}
