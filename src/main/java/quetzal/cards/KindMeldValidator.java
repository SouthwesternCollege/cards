package quetzal.cards;

import java.util.Comparator;
import java.util.List;

public final class KindMeldValidator implements MeldValidator {

    @Override
    public MeldValidationResult validate(List<Card> cards) {
        MeldValidationResult commonFailure = commonValidationFailure(cards);
        if (commonFailure != null) {
            return commonFailure;
        }

        List<Card> naturalCards = cards.stream()
                .filter(card -> !card.isJoker())
                .toList();

        Rank rank = naturalCards.getFirst().rank();

        boolean mixedRanks = naturalCards.stream()
                .anyMatch(card -> card.rank() != rank);

        if (mixedRanks) {
            return MeldValidationResult.invalid(MeldValidationError.MIXED_RANKS);
        }

        List<JokerAssignment> jokerAssignments = cards.stream()
                .filter(Card::isJoker)
                .map(card -> new JokerAssignment(card.id(), rank, null))
                .toList();

        List<Card> normalizedCards = cards.stream()
                .sorted(Comparator
                        .comparing((Card card) -> card.isJoker())
                        .thenComparing(card -> card.suit(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(card -> card.id().value()))
                .toList();

        return MeldValidationResult.valid(MeldType.KIND, normalizedCards, jokerAssignments);
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
}
