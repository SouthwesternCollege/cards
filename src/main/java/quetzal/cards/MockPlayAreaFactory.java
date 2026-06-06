package quetzal.cards;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Development-only mock data factory for testing full-table layout before real
 * GameState / PlayArea exists.
 */
public final class MockPlayAreaFactory {

    private int nextCardId = 50_000;

    public Map<PlayerId, List<VisualMeld>> createMockMelds(int playerCount) {
        Map<PlayerId, List<VisualMeld>> result = new LinkedHashMap<>();

        for (int i = 1; i <= playerCount; i++) {
            PlayerId playerId = new PlayerId(i);
            result.put(playerId, createPlayerMelds(playerId, i));
        }

        return result;
    }

    private List<VisualMeld> createPlayerMelds(PlayerId playerId, int playerIndex) {
        List<VisualMeld> melds = new ArrayList<>();

        melds.add(kindMeld(playerId, Rank.values()[(playerIndex + 1) % Rank.values().length], 3));
        melds.add(straightFlush(playerId, Suit.values()[playerIndex % Suit.values().length], 1 + playerIndex, 4));
        melds.add(kindMeld(playerId, Rank.values()[(playerIndex + 5) % Rank.values().length], 4));
        melds.add(straightFlush(playerId, Suit.values()[(playerIndex + 2) % Suit.values().length], 3, 5));
        melds.add(kindMeld(playerId, Rank.values()[(playerIndex + 8) % Rank.values().length], 3));
        melds.add(straightFlush(playerId, Suit.values()[(playerIndex + 1) % Suit.values().length], 6, 4));

        if (playerIndex % 2 == 0) {
            melds.add(kindMeld(playerId, Rank.ACE, 5));
            melds.add(straightFlush(playerId, Suit.CLUB, 8, 5));
        }

        return melds;
    }

    private VisualMeld kindMeld(PlayerId playerId, Rank rank, int count) {
        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            cards.add(Card.standard(new CardId(nextCardId++), rank, Suit.values()[i % Suit.values().length]));
        }

        return new VisualMeld(playerId, cards);
    }

    private VisualMeld straightFlush(PlayerId playerId, Suit suit, int startSequenceValue, int count) {
        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            cards.add(Card.standard(new CardId(nextCardId++), rankForSequence(startSequenceValue + i), suit));
        }

        return new VisualMeld(playerId, cards);
    }

    private Rank rankForSequence(int sequenceValue) {
        int normalized = ((sequenceValue - 1) % Rank.values().length) + 1;

        for (Rank rank : Rank.values()) {
            if (rank.sequenceValue() == normalized) {
                return rank;
            }
        }

        throw new IllegalArgumentException("Unsupported sequence value: " + sequenceValue);
    }
}
