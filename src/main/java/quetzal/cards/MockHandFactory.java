package quetzal.cards;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Development-only mock hand data.
 *
 * This is not gameplay state. It exists only to test hot-seat privacy and debug
 * hand presentation before real multiplayer GameState exists.
 */
public final class MockHandFactory {

    private int nextCardId = 70_000;

    public Map<PlayerId, List<Card>> createMockHands(int playerCount, int cardsPerHand) {
        Map<PlayerId, List<Card>> result = new LinkedHashMap<>();

        for (int playerNumber = 1; playerNumber <= playerCount; playerNumber++) {
            PlayerId playerId = new PlayerId(playerNumber);
            result.put(playerId, createHand(playerNumber, cardsPerHand));
        }

        return result;
    }

    private List<Card> createHand(int playerNumber, int cardsPerHand) {
        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < cardsPerHand; i++) {
            if (i == cardsPerHand - 1 && playerNumber % 2 == 0) {
                cards.add(Card.joker(new CardId(nextCardId++)));
            } else {
                Rank rank = Rank.values()[(i + playerNumber * 2) % Rank.values().length];
                Suit suit = Suit.values()[(i + playerNumber) % Suit.values().length];
                cards.add(Card.standard(new CardId(nextCardId++), rank, suit));
            }
        }

        return cards;
    }
}
