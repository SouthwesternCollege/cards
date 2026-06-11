package quetzal.cards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Round-specific opening requirement.
 *
 * This class evaluates melds already created by a player. Because the current UI
 * creates one meld at a time, multi-meld openings can be built across multiple
 * CreateMeldAction calls during the meld phase.
 */
public final class OpeningRequirement {

    private final int roundNumber;
    private final MeldType requiredMeldType;
    private final int requiredMeldCount;
    private final int requiredCardCountPerMeld;

    private OpeningRequirement(int roundNumber, MeldType requiredMeldType, int requiredMeldCount, int requiredCardCountPerMeld) {
        if (roundNumber < 1 || roundNumber > 6) {
            throw new IllegalArgumentException("Round number must be 1-6.");
        }

        if (requiredMeldType == null) {
            throw new IllegalArgumentException("Required meld type cannot be null.");
        }

        if (requiredMeldCount <= 0) {
            throw new IllegalArgumentException("Required meld count must be positive.");
        }

        if (requiredCardCountPerMeld < 3) {
            throw new IllegalArgumentException("Required card count must be at least three.");
        }

        this.roundNumber = roundNumber;
        this.requiredMeldType = requiredMeldType;
        this.requiredMeldCount = requiredMeldCount;
        this.requiredCardCountPerMeld = requiredCardCountPerMeld;
    }

    public static List<OpeningRequirement> all() {
        return List.of(
                forRound(1),
                forRound(2),
                forRound(3),
                forRound(4),
                forRound(5),
                forRound(6)
        );
    }

    public static OpeningRequirement forRound(int roundNumber) {
        return switch (roundNumber) {
            case 1 -> new OpeningRequirement(1, MeldType.KIND, 1, 3);
            case 2 -> new OpeningRequirement(2, MeldType.KIND, 2, 3);
            case 3 -> new OpeningRequirement(3, MeldType.KIND, 1, 4);
            case 4 -> new OpeningRequirement(4, MeldType.KIND, 2, 4);
            case 5 -> new OpeningRequirement(5, MeldType.KIND, 1, 5);
            case 6 -> new OpeningRequirement(6, MeldType.STRAIGHT_FLUSH, 1, 8);
            default -> throw new IllegalArgumentException("Round number must be 1-6.");
        };
    }

    public int roundNumber() {
        return roundNumber;
    }

    public MeldType requiredMeldType() {
        return requiredMeldType;
    }

    public int requiredMeldCount() {
        return requiredMeldCount;
    }

    public int requiredCardCountPerMeld() {
        return requiredCardCountPerMeld;
    }

    public boolean acceptsAsOpeningMeld(MeldState meld) {
        if (meld == null) {
            return false;
        }

        return meld.meldType() == requiredMeldType
                && meld.cards().size() >= requiredCardCountPerMeld;
    }

    public boolean isSatisfiedBy(List<MeldState> playerMelds) {
        if (playerMelds == null) {
            return false;
        }

        List<MeldState> qualifyingMelds = new ArrayList<>();

        for (MeldState meld : playerMelds) {
            if (acceptsAsOpeningMeld(meld)) {
                qualifyingMelds.add(meld);
            }
        }

        qualifyingMelds.sort(Comparator.comparingInt((MeldState meld) -> meld.cards().size()).reversed());
        return qualifyingMelds.size() >= requiredMeldCount;
    }

    public String description() {
        return displayText();
    }

    public boolean isSatisfiedByValidationResults(List<MeldValidationResult> newlyCreatedMelds) {
        if (newlyCreatedMelds == null) {
            return false;
        }

        long matchingMelds = newlyCreatedMelds.stream()
                .filter(MeldValidationResult::valid)
                .filter(result -> result.meldType() == requiredMeldType)
                .filter(result -> result.cardCount() >= requiredCardCountPerMeld)
                .count();

        return matchingMelds >= requiredMeldCount;
    }

    public String displayText() {
        String meldText = switch (requiredMeldType) {
            case KIND -> requiredCardCountPerMeld + "-of-a-kind";
            case STRAIGHT_FLUSH -> "straight flush of " + requiredCardCountPerMeld;
        };

        if (requiredMeldCount == 1) {
            return "Round " + roundNumber + " opening: one " + meldText + ".";
        }

        return "Round " + roundNumber + " opening: " + requiredMeldCount + " " + meldText + " melds.";
    }
}
