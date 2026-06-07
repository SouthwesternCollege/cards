package quetzal.cards;

/**
 * Small rules helper for turn-phase legality.
 *
 * This intentionally covers only the simple turn loop:
 *
 * DRAW_OR_CASTIGO -> MELD -> DISCARD / next player's DRAW_OR_CASTIGO
 *
 * Castigo timing, opening requirements, stolen joker obligations, and round-end
 * detection are intentionally later Milestone 6 slices.
 */
public final class TurnRules {

    private TurnRules() {
    }

    public static ActionResult requireActivePlayer(GameState state, PlayerId playerId, String actionName) {
        if (!playerId.equals(state.roundState().activePlayerId())) {
            return ActionResult.failure(
                    ActionFailureCode.NOT_ACTIVE_PLAYER,
                    "Only the active player may " + actionName + "."
            );
        }

        return null;
    }

    public static ActionResult requirePhase(GameState state, TurnPhase requiredPhase, String actionName) {
        TurnPhase currentPhase = state.roundState().turnPhase();

        if (currentPhase != requiredPhase) {
            return ActionResult.failure(
                    ActionFailureCode.WRONG_TURN_PHASE,
                    "Cannot " + actionName + " during phase: " + currentPhase + ". Required phase: " + requiredPhase + "."
            );
        }

        return null;
    }

    public static ActionResult requireActivePlayerInPhase(
            GameState state,
            PlayerId playerId,
            TurnPhase requiredPhase,
            String actionName
    ) {
        ActionResult activePlayerResult = requireActivePlayer(state, playerId, actionName);

        if (activePlayerResult != null) {
            return activePlayerResult;
        }

        return requirePhase(state, requiredPhase, actionName);
    }
}
