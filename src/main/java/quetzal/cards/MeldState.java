package quetzal.cards;

import java.util.List;

/**
 * Rules-level meld state.
 *
 * Unlike VisualMeld, this belongs to GameState. It represents a meld that
 * exists in the game rules, not merely how cards are drawn.
 */
public record MeldState(
        PlayerId createdBy,
        MeldType meldType,
        List<Card> cards
) {

    public MeldState {
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
}
