package quetzal.cards;

import java.util.List;

public interface MeldValidator {

    MeldValidationResult validate(List<Card> cards);
}
