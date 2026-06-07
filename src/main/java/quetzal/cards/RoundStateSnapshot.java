package quetzal.cards;

public record RoundStateSnapshot(
        int roundNumber,
        int dealerPlayerId,
        int activePlayerId,
        String turnPhase
) {

    public RoundStateSnapshot {
        if (roundNumber < 1 || roundNumber > 6) {
            throw new IllegalArgumentException("Round number must be 1-6.");
        }

        if (dealerPlayerId <= 0) {
            throw new IllegalArgumentException("Dealer player id must be positive.");
        }

        if (activePlayerId <= 0) {
            throw new IllegalArgumentException("Active player id must be positive.");
        }

        if (turnPhase == null || turnPhase.isBlank()) {
            throw new IllegalArgumentException("Turn phase cannot be blank.");
        }
    }
}
