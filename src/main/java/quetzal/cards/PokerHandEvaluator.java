package quetzal.cards;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PokerHandEvaluator {

    // Card suits and ranks
    private static final String[] SUITS = {"Hearts", "Diamonds", "Clubs", "Spades"};
    private static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

    // Poker hand rankings
    private enum HandRank {
        HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH;
    }

    // Get rank of card from 0 to 51
    private static int getRank(int card) {
        return card % 13;
    }

    // Get suit of card from 0 to 51
    private static int getSuit(int card) {
        return card / 13;
    }

    // Sort hand by rank
    private static int[] sortHand(int[] hand) {
        return Arrays.stream(hand).map(PokerHandEvaluator::getRank).sorted().toArray();
    }

    // Check for flush (all cards have the same suit)
    private static boolean isFlush(int[] hand) {
        int suit = getSuit(hand[0]);
        for (int card : hand) {
            if (getSuit(card) != suit) {
                return false;
            }
        }
        return true;
    }

    // Check for straight (consecutive ranks)
    private static boolean isStraight(int[] hand) {
        int[] sortedRanks = sortHand(hand);
        for (int i = 1; i < sortedRanks.length; i++) {
            if (sortedRanks[i] != sortedRanks[i - 1] + 1) {
                return false;
            }
        }
        // Special case for Ace-low straight (A, 2, 3, 4, 5)
        return !(sortedRanks[sortedRanks.length - 1] == 12 && sortedRanks[0] == 0);
    }

    // Count occurrences of each rank
    private static Map<Integer, Integer> countRanks(int[] hand) {
        Map<Integer, Integer> rankCount = new HashMap<>();
        for (int card : hand) {
            int rank = getRank(card);
            rankCount.put(rank, rankCount.getOrDefault(rank, 0) + 1);
        }
        return rankCount;
    }

    // Determine the hand ranking for any hand size (from 2 to 5 cards)
    private static HandRank evaluateHand(int[] hand) {
        boolean flush = hand.length > 4 && isFlush(hand); // Flush needs at least 2 cards of the same suit
        boolean straight = hand.length > 4 && isStraight(hand); // Straight requires at least 3 cards
        Map<Integer, Integer> rankCount = countRanks(hand);
        int pairs = 0, threes = 0, fours = 0;

        // Count pairs, triples, and quads
        for (int count : rankCount.values()) {
            if (count == 2) pairs++;
            if (count == 3) threes++;
            if (count == 4) fours++;
        }

        // Identify the hand based on available cards
        if (flush && straight && hand.length == 5) return HandRank.STRAIGHT_FLUSH;
        if (fours == 1) return HandRank.FOUR_OF_A_KIND;
        if (threes == 1 && pairs == 1) return HandRank.FULL_HOUSE;
        if (flush) return HandRank.FLUSH;
        if (straight) return HandRank.STRAIGHT;
        if (threes == 1) return HandRank.THREE_OF_A_KIND;
        if (pairs == 2) return HandRank.TWO_PAIR;
        if (pairs == 1) return HandRank.ONE_PAIR;

        return HandRank.HIGH_CARD; // Default to high card if no patterns found
    }

    // Method to print out the hand ranking
    public static String rankHand(int[] hand) {
        if (hand.length == 0) {
            return "No cards in hand!";
        }

        HandRank handRank = evaluateHand(hand);
        String handDescription = "" + handRank;

        if (handRank == HandRank.HIGH_CARD) {
            int highestCard = Arrays.stream(hand).map(PokerHandEvaluator::getRank).max().getAsInt();
            handDescription += ": " + RANKS[highestCard];
        }

        return handDescription;
    }

    public static void main(String[] args) {
        // Example with 2 cards (pair detection)
        int[] hand1 = {0, 13}; // Two of Hearts, Two of Diamonds
        System.out.println(rankHand(hand1)); // Outputs "One Pair"

        // Example with 3 cards (three-of-a-kind detection)
        int[] hand2 = {0, 13, 26}; // Two of Hearts, Two of Diamonds, Two of Clubs
        System.out.println(rankHand(hand2)); // Outputs "Three of a Kind"

        // Example with 4 cards (high card)
        int[] hand3 = {0, 13, 26, 39}; // All Twos (Hearts, Diamonds, Clubs, Spades)
        System.out.println(rankHand(hand3)); // Outputs "Four of a Kind"

        // Full hand example
        int[] hand4 = {0, 12, 25, 38, 51}; // Two of Hearts, Ace of Hearts, King of Diamonds, Queen of Clubs, Ace of Spades
        System.out.println(rankHand(hand4)); // Outputs the hand ranking
    }
}
