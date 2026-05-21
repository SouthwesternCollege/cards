package quetzal.cards;

import java.util.List;

public final class OpeningRequirements {

    private OpeningRequirements() {
    }

    public static List<OpeningRequirement> all() {
        return List.of(
                kindRequirement(1, 1, 3, "One three-of-a-kind"),
                kindRequirement(2, 2, 3, "Two three-of-a-kind melds"),
                kindRequirement(3, 1, 4, "One four-of-a-kind"),
                kindRequirement(4, 2, 4, "Two four-of-a-kind melds"),
                kindRequirement(5, 1, 5, "One five-of-a-kind"),
                straightFlushRequirement(6, 8, "One straight flush of eight cards")
        );
    }

    public static OpeningRequirement forRound(int roundNumber) {
        return all().stream()
                .filter(requirement -> requirement.roundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No opening requirement for round: " + roundNumber));
    }

    private static OpeningRequirement kindRequirement(int roundNumber, int requiredMeldCount, int minimumCards, String description) {
        return new OpeningRequirement() {
            @Override
            public int roundNumber() {
                return roundNumber;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public boolean isSatisfiedBy(List<MeldValidationResult> newlyCreatedMelds) {
                if (newlyCreatedMelds == null) {
                    return false;
                }

                long matchingMelds = newlyCreatedMelds.stream()
                        .filter(MeldValidationResult::valid)
                        .filter(result -> result.meldType() == MeldType.KIND)
                        .filter(result -> result.cardCount() >= minimumCards)
                        .count();

                return matchingMelds >= requiredMeldCount;
            }
        };
    }

    private static OpeningRequirement straightFlushRequirement(int roundNumber, int minimumCards, String description) {
        return new OpeningRequirement() {
            @Override
            public int roundNumber() {
                return roundNumber;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public boolean isSatisfiedBy(List<MeldValidationResult> newlyCreatedMelds) {
                if (newlyCreatedMelds == null) {
                    return false;
                }

                return newlyCreatedMelds.stream()
                        .filter(MeldValidationResult::valid)
                        .anyMatch(result -> result.meldType() == MeldType.STRAIGHT_FLUSH && result.cardCount() >= minimumCards);
            }
        };
    }
}
