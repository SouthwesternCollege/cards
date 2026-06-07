package quetzal.cards;

/**
 * Rules-level state for the current round.
 */
public final class RoundState {

    private final int roundNumber;
    private PlayerId dealerId;
    private PlayerId activePlayerId;
    private TurnPhase turnPhase;

    public RoundState(int roundNumber, PlayerId dealerId, PlayerId activePlayerId, TurnPhase turnPhase) {
        if (roundNumber < 1 || roundNumber > 6) {
            throw new IllegalArgumentException("La Kika has six rounds; round number must be 1-6.");
        }

        if (dealerId == null) {
            throw new IllegalArgumentException("Dealer id cannot be null.");
        }

        if (activePlayerId == null) {
            throw new IllegalArgumentException("Active player id cannot be null.");
        }

        if (turnPhase == null) {
            throw new IllegalArgumentException("Turn phase cannot be null.");
        }

        this.roundNumber = roundNumber;
        this.dealerId = dealerId;
        this.activePlayerId = activePlayerId;
        this.turnPhase = turnPhase;
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

    public TurnPhase turnPhase() {
        return turnPhase;
    }

    public void setDealerId(PlayerId dealerId) {
        if (dealerId == null) {
            throw new IllegalArgumentException("Dealer id cannot be null.");
        }

        this.dealerId = dealerId;
    }

    public void setActivePlayerId(PlayerId activePlayerId) {
        if (activePlayerId == null) {
            throw new IllegalArgumentException("Active player id cannot be null.");
        }

        this.activePlayerId = activePlayerId;
    }

    public void setTurnPhase(TurnPhase turnPhase) {
        if (turnPhase == null) {
            throw new IllegalArgumentException("Turn phase cannot be null.");
        }

        this.turnPhase = turnPhase;
    }
}
