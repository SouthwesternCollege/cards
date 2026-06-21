package quetzal.cards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private List<CardId> customOrderCardIds = new ArrayList<>();
    private final List<StolenJokerObligation> stolenJokerObligations = new ArrayList<>();

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

    public List<StolenJokerObligation> stolenJokerObligations() {
        return List.copyOf(stolenJokerObligations);
    }

    public boolean hasStolenJokerObligations() {
        return !stolenJokerObligations.isEmpty();
    }

    public void addStolenJokerObligation(StolenJokerObligation obligation) {
        if (obligation == null) {
            throw new IllegalArgumentException("Obligation cannot be null.");
        }

        stolenJokerObligations.add(obligation);
    }

    public void clearStolenJokerObligations() {
        stolenJokerObligations.clear();
    }

    public boolean hasObligationForJoker(CardId jokerId) {
        for (StolenJokerObligation obligation : stolenJokerObligations) {
            if (obligation.joker().id().equals(jokerId)) {
                return true;
            }
        }

        return false;
    }

    public void clearObligationsForCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        Set<CardId> cardIds = cards.stream()
                .map(Card::id)
                .collect(java.util.stream.Collectors.toSet());
        stolenJokerObligations.removeIf(obligation -> cardIds.contains(obligation.joker().id()));
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

    public List<Card> cardsByIdInOrder(List<CardId> cardIds) {
        List<Card> result = new ArrayList<>();

        for (CardId cardId : cardIds) {
            result.add(cardById(cardId));
        }

        return result;
    }

    public Card cardById(CardId cardId) {
        for (Card card : hand) {
            if (card.id().equals(cardId)) {
                return card;
            }
        }

        throw new IllegalArgumentException("Card is not in player's hand: " + cardId.value());
    }


    public List<CardId> handOrderIds() {
        List<CardId> ids = new ArrayList<>();

        for (Card card : hand) {
            ids.add(card.id());
        }

        return List.copyOf(ids);
    }

    public void saveCurrentHandOrderAsCustom() {
        customOrderCardIds = new ArrayList<>(handOrderIds());
    }

    public void restoreCustomHandOrder() {
        reorderByCustomOrder(customOrderCardIds);
    }

    public void reorderHand(List<CardId> orderedCardIds) {
        if (orderedCardIds == null) {
            throw new IllegalArgumentException("Ordered card ids cannot be null.");
        }

        if (orderedCardIds.size() != hand.size()) {
            throw new IllegalArgumentException("Ordered card ids must match the current hand size.");
        }

        Set<CardId> currentIds = new HashSet<>(handOrderIds());
        Set<CardId> requestedIds = new HashSet<>(orderedCardIds);

        if (!currentIds.equals(requestedIds)) {
            throw new IllegalArgumentException("Ordered card ids must contain exactly the cards in the player's hand.");
        }

        List<Card> reordered = cardsByIdInOrder(orderedCardIds);
        hand.clear();
        hand.addAll(reordered);
    }

    public void sortHandByRank() {
        hand.sort(
                Comparator.comparingInt((Card card) -> card.isJoker() ? Integer.MAX_VALUE : card.rank().sequenceValue())
                        .thenComparing(card -> card.isJoker() ? null : card.suit(), Comparator.nullsLast(Comparator.naturalOrder()))
        );
    }

    public void sortHandBySuit() {
        hand.sort(
                Comparator.comparing((Card card) -> card.isJoker() ? null : card.suit(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(card -> card.isJoker() ? Integer.MAX_VALUE : card.rank().sequenceValue())
        );
    }

    private void reorderByCustomOrder(List<CardId> savedOrder) {
        List<Card> reordered = new ArrayList<>();
        Set<CardId> added = new HashSet<>();

        for (CardId cardId : savedOrder) {
            for (Card card : hand) {
                if (card.id().equals(cardId)) {
                    reordered.add(card);
                    added.add(card.id());
                    break;
                }
            }
        }

        for (Card card : hand) {
            if (!added.contains(card.id())) {
                reordered.add(card);
            }
        }

        hand.clear();
        hand.addAll(reordered);
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

    public void consumeCastigo() {
        if (castigosRemaining <= 0) {
            throw new IllegalStateException("No castigos remaining.");
        }

        castigosRemaining--;
    }

    public PlayerStateSnapshot toSnapshot() {
        return new PlayerStateSnapshot(
                playerId.value(),
                displayName,
                hand.stream()
                        .map(Card::toSnapshot)
                        .toList(),
                cumulativeScore,
                castigosRemaining,
                opened,
                customOrderCardIds.stream()
                        .map(CardId::value)
                        .toList()
        );
    }

    public static PlayerState fromSnapshot(PlayerStateSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Player snapshot cannot be null.");
        }

        PlayerState player = new PlayerState(
                new PlayerId(snapshot.playerId()),
                snapshot.displayName(),
                snapshot.cumulativeScore(),
                snapshot.castigosRemaining(),
                snapshot.opened()
        );

        for (CardSnapshot cardSnapshot : snapshot.hand()) {
            player.addCard(Card.fromSnapshot(cardSnapshot));
        }

        player.customOrderCardIds = snapshot.customOrderCardIds().stream()
                .map(CardId::new)
                .toList();

        return player;
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
