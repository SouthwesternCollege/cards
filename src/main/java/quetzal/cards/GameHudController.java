package quetzal.cards;

/**
 * Coordinates changes to HUD presentation state and asks GameHUD to render them.
 *
 * This keeps GameHUD closer to a view and avoids static HUD update calls spread
 * through gameplay/presentation components.
 */
public final class GameHudController {

    private final PlayerHudModel model;
    private final GameHUD hud;

    public GameHudController(PlayerHudModel model, GameHUD hud) {
        if (model == null) {
            throw new IllegalArgumentException("Player HUD model cannot be null.");
        }

        if (hud == null) {
            throw new IllegalArgumentException("Game HUD cannot be null.");
        }

        this.model = model;
        this.hud = hud;
    }

    public void refresh() {
        hud.setPlayerStates(model.playerStates());
        hud.setSelectedMeld(model.selectedMeldText());
    }

    public void setSelectedMeld(String selectedMeld) {
        model.setSelectedMeldText(selectedMeld);
        hud.setSelectedMeld(model.selectedMeldText());
    }

    public void setCardsRemaining(PlayerId playerId, int cardsRemaining) {
        model.setCardsRemaining(playerId, cardsRemaining);
        hud.setPlayerStates(model.playerStates());
    }
}
