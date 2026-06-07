package quetzal.cards;

import java.util.List;

public record GameStateSnapshot(
        List<PlayerStateSnapshot> players,
        DeckSnapshot deck,
        DiscardPileSnapshot discardPile,
        PlayAreaSnapshot playArea,
        RoundStateSnapshot roundState
) {

    public GameStateSnapshot {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players cannot be empty.");
        }

        if (deck == null) {
            throw new IllegalArgumentException("Deck snapshot cannot be null.");
        }

        if (discardPile == null) {
            throw new IllegalArgumentException("Discard pile snapshot cannot be null.");
        }

        if (playArea == null) {
            throw new IllegalArgumentException("Play area snapshot cannot be null.");
        }

        if (roundState == null) {
            throw new IllegalArgumentException("Round state snapshot cannot be null.");
        }

        players = List.copyOf(players);
    }
}
