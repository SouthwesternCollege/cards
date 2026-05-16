package quetzal.cards;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerHandEvaluator {

    private enum HandRank {
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH
    }

    public static String rankHand(List<Card> hand) {
        if (hand.isEmpty()) {
            return "No cards in hand!";
        }

        if (hand.stream().anyMatch(Card::isJoker)) {
            return "Wild-card evaluation not implemented yet";
        }

        HandRank handRank = evaluateHand(hand);
        String handDescription = handRank.toString();

        if (handRank == HandRank.HIGH_CARD) {
            Rank highestRank = hand.stream()
                    .map(Card::rank)
                    .max(Comparator.comparingInt(Rank::sequenceValue))
                    .orElseThrow();

            handDescription += ": " + highestRank;
        }

        return handDescription;
    }

    private static HandRank evaluateHand(List<Card> hand) {
        boolean flush = hand.size() == 5 && isFlush(hand);
        boolean straight = hand.size() == 5 && isStraight(hand);
        Map<Rank, Integer> rankCount = countRanks(hand);

        int pairs = 0;
        int threes = 0;
        int fours = 0;

        for (int count : rankCount.values()) {
            if (count == 2) {
                pairs++;
            }
            if (count == 3) {
                threes++;
            }
            if (count == 4) {
                fours++;
            }
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
        Suit suit = hand.getFirst().suit();

        for (Card card : hand) {
            if (card.suit() != suit) {
                return false;
            }
        }

        return true;
    }

    private static boolean isStraight(List<Card> hand) {
        List<Integer> sortedValues = hand.stream()
                .map(card -> card.rank().sequenceValue())
                .sorted()
                .toList();

        boolean normalStraight = true;
        for (int i = 1; i < sortedValues.size(); i++) {
            if (sortedValues.get(i) != sortedValues.get(i - 1) + 1) {
                normalStraight = false;
                break;
            }
        }

        if (normalStraight) {
            return true;
        }

        return false;
    }

    private static Map<Rank, Integer> countRanks(List<Card> hand) {
        Map<Rank, Integer> rankCount = new HashMap<>();

        for (Card card : hand) {
            Rank rank = card.rank();
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }

        return rankCount;
    }
}
