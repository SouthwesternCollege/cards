package quetzal.cards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class StraightFlushMeldValidator implements MeldValidator {

    private static final int LOWEST_SEQUENCE_VALUE = Rank.ACE.sequenceValue();
    private static final int HIGHEST_SEQUENCE_VALUE = Rank.KING.sequenceValue();

    @Override
    public MeldValidationResult validate(List<Card> cards) {
        MeldValidationResult commonFailure = commonValidationFailure(cards);
        if (commonFailure != null) {
            return commonFailure;
        }

        List<Card> naturalCards = cards.stream()
                .filter(card -> !card.isJoker())
                .toList();

        Suit suit = naturalCards.getFirst().suit();

        boolean mixedSuits = naturalCards.stream()
                .anyMatch(card -> card.suit() != suit);

        if (mixedSuits) {
            return MeldValidationResult.invalid(MeldValidationError.MIXED_SUITS);
        }

        Set<Integer> naturalRankValues = new HashSet<>();
        for (Card card : naturalCards) {
            if (!naturalRankValues.add(card.rank().sequenceValue())) {
                return MeldValidationResult.invalid(MeldValidationError.DUPLICATE_SEQUENCE_RANK);
            }
        }

        int cardCount = cards.size();
        int maxStart = HIGHEST_SEQUENCE_VALUE - cardCount + 1;

        for (int start = LOWEST_SEQUENCE_VALUE; start <= maxStart; start++) {
            int end = start + cardCount - 1;

            if (!sequenceContainsAllNaturalRanks(naturalRankValues, start, end)) {
                continue;
            }

            List<Integer> missingRankValues = missingRankValues(naturalRankValues, start, end);

            if (missingRankValues.size() != JokerRules.countJokers(cards)) {
                continue;
            }

            List<JokerAssignment> jokerAssignments = assignJokers(cards, missingRankValues, suit);

            if (JokerRules.hasConsecutiveJokerAssignments(jokerAssignments)) {
                return MeldValidationResult.invalid(MeldValidationError.CONSECUTIVE_JOKERS);
            }

            List<Card> normalizedCards = normalizeStraightFlushCards(cards, start, end, jokerAssignments);

            return MeldValidationResult.valid(MeldType.STRAIGHT_FLUSH, normalizedCards, jokerAssignments);
        }

        return MeldValidationResult.invalid(MeldValidationError.NO_CONSECUTIVE_SEQUENCE);
    }

    private MeldValidationResult commonValidationFailure(List<Card> cards) {
        if (cards == null || cards.size() < 3) {
            return MeldValidationResult.invalid(MeldValidationError.TOO_FEW_CARDS);
        }

        if (cards.stream().anyMatch(card -> card == null)) {
            return MeldValidationResult.invalid(MeldValidationError.NULL_CARD);
        }

        if (!JokerRules.hasLegalJokerRatio(cards)) {
            return MeldValidationResult.invalid(MeldValidationError.TOO_MANY_JOKERS);
        }

        if (!JokerRules.hasAtLeastOneNaturalCard(cards)) {
            return MeldValidationResult.invalid(MeldValidationError.ALL_JOKERS);
        }

        return null;
    }

    private boolean sequenceContainsAllNaturalRanks(Set<Integer> naturalRankValues, int start, int end) {
        return naturalRankValues.stream()
                .allMatch(value -> value >= start && value <= end);
    }

    private List<Integer> missingRankValues(Set<Integer> naturalRankValues, int start, int end) {
        List<Integer> missing = new ArrayList<>();

        for (int value = start; value <= end; value++) {
            if (!naturalRankValues.contains(value)) {
                missing.add(value);
            }
        }

        return missing;
    }

    private List<JokerAssignment> assignJokers(List<Card> cards, List<Integer> missingRankValues, Suit suit) {
        List<Card> jokers = cards.stream()
                .filter(Card::isJoker)
                .sorted(Comparator.comparingInt(card -> card.id().value()))
                .toList();

        List<JokerAssignment> assignments = new ArrayList<>();

        for (int i = 0; i < jokers.size(); i++) {
            assignments.add(new JokerAssignment(
                    jokers.get(i).id(),
                    rankBySequenceValue(missingRankValues.get(i)),
                    suit
            ));
        }

        return assignments;
    }

    private List<Card> normalizeStraightFlushCards(List<Card> cards, int start, int end, List<JokerAssignment> jokerAssignments) {
        List<Card> normalized = new ArrayList<>();

        for (int value = start; value <= end; value++) {
            int rankValue = value;

            cards.stream()
                    .filter(card -> !card.isJoker())
                    .filter(card -> card.rank().sequenceValue() == rankValue)
                    .findFirst()
                    .ifPresentOrElse(
                            normalized::add,
                            () -> jokerAssignments.stream()
                                    .filter(assignment -> assignment.assignedRank().sequenceValue() == rankValue)
                                    .findFirst()
                                    .flatMap(assignment -> cards.stream()
                                            .filter(Card::isJoker)
                                            .filter(card -> card.id().equals(assignment.jokerId()))
                                            .findFirst())
                                    .ifPresent(normalized::add)
                    );
        }

        return normalized;
    }

    private Rank rankBySequenceValue(int sequenceValue) {
        for (Rank rank : Rank.values()) {
            if (rank.sequenceValue() == sequenceValue) {
                return rank;
            }
        }

        throw new IllegalArgumentException("No rank for sequence value: " + sequenceValue);
    }
}
