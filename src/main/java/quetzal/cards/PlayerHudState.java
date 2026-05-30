package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

public record PlayerHudState(
        PlayerId playerId,
        String displayName,
        int cumulativeScore,
        int cardsRemaining,
        int castigosRemaining,
        boolean opened,
        boolean dealer,
        boolean activeTurn
) {

    public PlayerHudState {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }

        if (cumulativeScore < -9999) {
            throw new IllegalArgumentException("Cumulative score is suspiciously low.");
        }

        if (cardsRemaining < 0) {
            throw new IllegalArgumentException("Cards remaining cannot be negative.");
        }

        if (castigosRemaining < 0) {
            throw new IllegalArgumentException("Castigos remaining cannot be negative.");
        }
    }

    public PlayerHudState withCardsRemaining(int cardsRemaining) {
        return new PlayerHudState(
                playerId,
                displayName,
                cumulativeScore,
                cardsRemaining,
                castigosRemaining,
                opened,
                dealer,
                activeTurn
        );
    }

    public PlayerHudState withSelectedTurnState(boolean dealer, boolean activeTurn) {
        return new PlayerHudState(
                playerId,
                displayName,
                cumulativeScore,
                cardsRemaining,
                castigosRemaining,
                opened,
                dealer,
                activeTurn
        );
    }

    public PlayerHudState withOpened(boolean opened) {
        return new PlayerHudState(
                playerId,
                displayName,
                cumulativeScore,
                cardsRemaining,
                castigosRemaining,
                opened,
                dealer,
                activeTurn
        );
    }

    public PlayerHudState withCastigosRemaining(int castigosRemaining) {
        return new PlayerHudState(
                playerId,
                displayName,
                cumulativeScore,
                cardsRemaining,
                castigosRemaining,
                opened,
                dealer,
                activeTurn
        );
    }

    public static List<PlayerHudState> prototypePlayers(int playerCount) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("La Kika supports 2-4 players.");
        }

        List<PlayerHudState> players = new ArrayList<>();

        for (int i = 1; i <= playerCount; i++) {
            PlayerId playerId = new PlayerId(i);
            players.add(new PlayerHudState(
                    playerId,
                    "Player " + i,
                    0,
                    i == 1 ? 0 : 13,
                    10,
                    i == 1,
                    i == 1,
                    i == 1
            ));
        }

        return players;
    }
}
