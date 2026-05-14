package quetzal.cards;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerHandEvaluator {

    private enum HandRank {
        HIGH_CARD("High Card"),
        ONE_PAIR("One Pair"),
        TWO_PAIR("Two Pair"),
        THREE_OF_A_KIND("Three of a Kind"),
        STRAIGHT("Straight"),
        FLUSH("Flush"),
        FULL_HOUSE("Full House"),
        FOUR_OF_A_KIND("Four of a Kind"),
        STRAIGHT_FLUSH("Straight Flush");

        private final String displayName;

        HandRank(String displayName) {
            this.displayName = displayName;
        }
    }

    public static String rankHand(List<Card> hand) {
        if (hand == null || hand.isEmpty()) {
            return "No cards in hand!";
        }

        if (hand.stream().anyMatch(Card::isJoker)) {
            return "Wild-card evaluation not implemented yet";
        }

        HandRank handRank = evaluateHand(hand);

        if (handRank == HandRank.HIGH_CARD) {
            Rank highestCard = hand.stream()
                    .map(Card::getRank)
                    .max((a, b) -> Integer.compare(a.pokerValue(), b.pokerValue()))
                    .orElseThrow();

            return handRank.displayName + ": " + highestCard.displayName();
        }

        return handRank.displayName;
    }

    public static String rankHand(int[] hand) {
        if (hand == null || hand.length == 0) {
            return "No cards in hand!";
        }

        return rankHand(Arrays.stream(hand)
                .mapToObj(Card::new)
                .toList());
    }

    private static HandRank evaluateHand(List<Card> hand) {
        boolean flush = hand.size() == 5 && isFlush(hand);
        boolean straight = hand.size() == 5 && isStraight(hand);

        Map<Rank, Integer> rankCount = countRanks(hand);
        int pairs = 0;
        int threes = 0;
        int fours = 0;

        for (int count : rankCount.values()) {
            if (count == 2) pairs++;
            if (count == 3) threes++;
            if (count == 4) fours++;
        }

        if (flush && straight) return HandRank.STRAIGHT_FLUSH;
        if (fours == 1) return HandRank.FOUR_OF_A_KIND;
        if (threes == 1 && pairs == 1) return HandRank.FULL_HOUSE;
        if (flush) return HandRank.FLUSH;
        if (straight) return HandRank.STRAIGHT;
        if (threes == 1) return HandRank.THREE_OF_A_KIND;
        if (pairs == 2) return HandRank.TWO_PAIR;
        if (pairs == 1) return HandRank.ONE_PAIR;

        return HandRank.HIGH_CARD;
    }

    private static boolean isFlush(List<Card> hand) {
        Suit suit = hand.getFirst().getSuit();

        for (Card card : hand) {
            if (card.getSuit() != suit) {
                return false;
            }
        }

        return true;
    }

    private static boolean isStraight(List<Card> hand) {
        int[] values = hand.stream()
                .map(Card::getRank)
                .mapToInt(Rank::pokerValue)
                .sorted()
                .toArray();

        if (Arrays.equals(values, new int[]{2, 3, 4, 5, 14})) {
            return true;
        }

        for (int i = 1; i < values.length; i++) {
            if (values[i] != values[i - 1] + 1) {
                return false;
            }
        }

        return true;
    }

    private static Map<Rank, Integer> countRanks(List<Card> hand) {
        Map<Rank, Integer> rankCount = new HashMap<>();

        for (Card card : hand) {
            Rank rank = card.getRank();
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }

        return rankCount;
    }
}
