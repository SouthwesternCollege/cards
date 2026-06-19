package quetzal.cards;

import java.util.List;

/**
 * Rules-level meld state.
 *
 * Unlike VisualMeld, this belongs to GameState. It represents a meld that
 * exists in the game rules, not merely how cards are drawn.
 */
public record MeldState(
        MeldId id,
        PlayerId createdBy,
        MeldType meldType,
        List<Card> cards
) {

    public MeldState {
        if (id == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (createdBy == null) {
            throw new IllegalArgumentException("Created-by player cannot be null.");
        }

        if (meldType == null) {
            throw new IllegalArgumentException("Meld type cannot be null.");
        }

        if (cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Meld cards cannot be empty.");
        }

        cards = List.copyOf(cards);
    }

    public MeldState(PlayerId createdBy, MeldType meldType, List<Card> cards) {
        this(new MeldId(1), createdBy, meldType, cards);
    }

    public MeldState withCards(List<Card> cards) {
        return new MeldState(id, createdBy, meldType, cards);
    }

    public MeldStateSnapshot toSnapshot() {
        return new MeldStateSnapshot(
                id.value(),
                createdBy.value(),
                meldType.name(),
                cards.stream()
                        .map(Card::toSnapshot)
                        .toList()
        );
    }

    public static MeldState fromSnapshot(MeldStateSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Meld snapshot cannot be null.");
        }

        return new MeldState(
                new MeldId(snapshot.meldId()),
                new PlayerId(snapshot.createdByPlayerId()),
                MeldType.valueOf(snapshot.meldType()),
                snapshot.cards().stream()
                        .map(Card::fromSnapshot)
                        .toList()
        );
    }
}
