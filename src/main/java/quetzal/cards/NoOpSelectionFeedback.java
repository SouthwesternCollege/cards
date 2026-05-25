package quetzal.cards;

import java.util.List;

public final class NoOpSelectionFeedback implements SelectionFeedback {

    @Override
    public void selectionChanged(List<Card> selectedCards) {
        // Intentionally empty.
    }

    @Override
    public void invalidPlayAttempt(MeldValidationResult validationResult) {
        // Intentionally empty.
    }
}
