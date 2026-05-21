package quetzal.cards;

import java.util.HashSet;
import java.util.Set;

public final class RoundState {

    private final int roundNumber;
    private final PlayerId dealerId;
    private final PlayerId activePlayerId;
    private final OpeningRequirement openingRequirement;
    private final Set<PlayerId> openedPlayers = new HashSet<>();
    private TurnPhase turnPhase;

    public RoundState(int roundNumber, PlayerId dealerId, PlayerId activePlayerId, OpeningRequirement openingRequirement) {
        if (roundNumber < 1 || roundNumber > 6) {
            throw new IllegalArgumentException("Round number must be between 1 and 6.");
        }

        if (dealerId == null) {
            throw new IllegalArgumentException("Dealer id cannot be null.");
        }

        if (activePlayerId == null) {
            throw new IllegalArgumentException("Active player id cannot be null.");
        }

        if (openingRequirement == null) {
            throw new IllegalArgumentException("Opening requirement cannot be null.");
        }

        this.roundNumber = roundNumber;
        this.dealerId = dealerId;
        this.activePlayerId = activePlayerId;
        this.openingRequirement = openingRequirement;
        this.turnPhase = TurnPhase.DRAW_OR_CASTIGO;
    }

    public int roundNumber() {
        return roundNumber;
    }

    public PlayerId dealerId() {
        return dealerId;
    }

    public PlayerId activePlayerId() {
        return activePlayerId;
    }

    public OpeningRequirement openingRequirement() {
        return openingRequirement;
    }

    public TurnPhase turnPhase() {
        return turnPhase;
    }

    public void setTurnPhase(TurnPhase turnPhase) {
        if (turnPhase == null) {
            throw new IllegalArgumentException("Turn phase cannot be null.");
        }

        this.turnPhase = turnPhase;
    }

    public boolean hasOpened(PlayerId playerId) {
        return openedPlayers.contains(playerId);
    }

    public void markOpened(PlayerId playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        openedPlayers.add(playerId);
    }
}
