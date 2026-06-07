package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Rules-level play area.
 *
 * Current Milestone 5E scope: store created melds. Mutation and joker stealing
 * come later.
 */
public final class PlayArea {

    private final List<MeldState> melds = new ArrayList<>();

    public void addMeld(MeldState meld) {
        if (meld == null) {
            throw new IllegalArgumentException("Meld cannot be null.");
        }

        melds.add(meld);
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
