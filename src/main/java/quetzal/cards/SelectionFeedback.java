package quetzal.cards;

import java.util.List;

public interface SelectionFeedback {

    void selectionChanged(List<Card> selectedCards);

    void invalidPlayAttempt(MeldValidationResult validationResult);
}
