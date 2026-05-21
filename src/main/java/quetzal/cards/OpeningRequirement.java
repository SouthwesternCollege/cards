package quetzal.cards;

import java.util.List;

public interface OpeningRequirement {

    int roundNumber();

    String description();

    boolean isSatisfiedBy(List<MeldValidationResult> newlyCreatedMelds);
}
