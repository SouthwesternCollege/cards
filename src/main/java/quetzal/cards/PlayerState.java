package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Rules-level state for one player.
 *
 * This class is domain state. It has no FXGL entities, no JavaFX nodes, and no
 * layout information.
 */
public final class PlayerState {

    private final PlayerId playerId;
    private final String displayName;
    private final List<Card> hand = new ArrayList<>();

    private int cumulativeScore;
    private int castigosRemaining;
    private boolean opened;

    public PlayerState(PlayerId playerId, String displayName, int cumulativeScore, int castigosRemaining, boolean opened) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null.");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name cannot be blank.");
        }

        if (castigosRemaining < 0) {
            throw new IllegalArgumentException("Castigos remaining cannot be negative.");
        }

        this.playerId = playerId;
        this.displayName = displayName;
        this.cumulativeScore = cumulativeScore;
        this.castigosRemaining = castigosRemaining;
        this.opened = opened;
    }

    public PlayerId playerId() {
        return playerId;
    }

    public String displayName() {
        return displayName;
    }

    public List<Card> hand() {
        return List.copyOf(hand);
    }

    public int handSize() {
        return hand.size();
    }

    public int cumulativeScore() {
        return cumulativeScore;
    }

    public int castigosRemaining() {
        return castigosRemaining;
    }

    public boolean opened() {
        return opened;
    }

    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }

        hand.add(card);
    }

    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    public Card removeCard(CardId cardId) {
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            if (card.id().equals(cardId)) {
                return hand.remove(i);
            }
        }

        throw new IllegalArgumentException("Card is not in player's hand: " + cardId.value());
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void setCumulativeScore(int cumulativeScore) {
        this.cumulativeScore = cumulativeScore;
    }

    public void setCastigosRemaining(int castigosRemaining) {
        if (castigosRemaining < 0) {
            throw new IllegalArgumentException("Castigos remaining cannot be negative.");
        }

        this.castigosRemaining = castigosRemaining;
    }

    public PlayerHudState toHudState(PlayerId dealerId, PlayerId activePlayerId) {
        return new PlayerHudState(
                playerId,
                displayName,
                cumulativeScore,
                handSize(),
                castigosRemaining,
                opened,
                playerId.equals(dealerId),
                playerId.equals(activePlayerId)
        );
    }
}
