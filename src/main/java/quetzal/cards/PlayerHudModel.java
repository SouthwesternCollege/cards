package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Presentation-facing HUD state.
 *
 * This is intentionally not the final game-state model. It gives the HUD one
 * clean source of truth while the real GameState / PlayerState layer is still
 * being developed.
 */
public final class PlayerHudModel {

    private final List<PlayerHudState> playerStates;
    private String selectedMeldText = "";

    public PlayerHudModel(List<PlayerHudState> playerStates) {
        if (playerStates == null || playerStates.isEmpty()) {
            throw new IllegalArgumentException("Player HUD states cannot be empty.");
        }

        this.playerStates = new ArrayList<>(playerStates);
    }

    public static PlayerHudModel prototype(int playerCount) {
        return new PlayerHudModel(PlayerHudState.prototypePlayers(playerCount));
    }

    public List<PlayerHudState> playerStates() {
        return List.copyOf(playerStates);
    }

    public String selectedMeldText() {
        return selectedMeldText;
    }

    public void setSelectedMeldText(String selectedMeldText) {
        this.selectedMeldText = selectedMeldText == null ? "" : selectedMeldText;
    }

    public void setCardsRemaining(PlayerId playerId, int cardsRemaining) {
        updatePlayer(playerId, state -> state.withCardsRemaining(cardsRemaining));
    }

    public void setPlayerState(PlayerHudState playerState) {
        if (playerState == null) {
            throw new IllegalArgumentException("Player HUD state cannot be null.");
        }

        updatePlayer(playerState.playerId(), ignored -> playerState);
    }

    private void updatePlayer(PlayerId playerId, PlayerHudStateUpdater updater) {
        for (int i = 0; i < playerStates.size(); i++) {
            PlayerHudState state = playerStates.get(i);

            if (state.playerId().equals(playerId)) {
                playerStates.set(i, updater.update(state));
                return;
            }
        }

        throw new IllegalArgumentException("Unknown player id: " + playerId.value());
    }

    @FunctionalInterface
    private interface PlayerHudStateUpdater {
        PlayerHudState update(PlayerHudState state);
    }
}
