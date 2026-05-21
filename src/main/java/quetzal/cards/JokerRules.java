package quetzal.cards;

import java.util.List;

public final class JokerRules {

    private JokerRules() {
    }

    public static int countJokers(List<Card> cards) {
        if (cards == null) {
            return 0;
        }

        return (int) cards.stream()
                .filter(Card::isJoker)
                .count();
    }

    public static boolean hasLegalJokerRatio(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }

        int jokerCount = countJokers(cards);
        return jokerCount <= cards.size() / 2;
    }

    public static boolean hasAtLeastOneNaturalCard(List<Card> cards) {
        if (cards == null) {
            return false;
        }

        return cards.stream().anyMatch(card -> !card.isJoker());
    }

    public static boolean hasConsecutiveJokerAssignments(List<JokerAssignment> assignments) {
        if (assignments == null || assignments.size() < 2) {
            return false;
        }

        List<Integer> assignedValues = assignments.stream()
                .map(assignment -> assignment.assignedRank().sequenceValue())
                .sorted()
                .toList();

        for (int i = 1; i < assignedValues.size(); i++) {
            if (assignedValues.get(i) == assignedValues.get(i - 1) + 1) {
                return true;
            }
        }

        return false;
    }
}
