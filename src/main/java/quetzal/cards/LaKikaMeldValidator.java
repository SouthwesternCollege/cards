package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

public final class LaKikaMeldValidator implements MeldValidator {

    private final MeldValidator kindValidator;
    private final MeldValidator straightFlushValidator;

    public LaKikaMeldValidator() {
        this(new KindMeldValidator(), new StraightFlushMeldValidator());
    }

    public LaKikaMeldValidator(MeldValidator kindValidator, MeldValidator straightFlushValidator) {
        if (kindValidator == null) {
            throw new IllegalArgumentException("Kind validator cannot be null.");
        }

        if (straightFlushValidator == null) {
            throw new IllegalArgumentException("Straight flush validator cannot be null.");
        }

        this.kindValidator = kindValidator;
        this.straightFlushValidator = straightFlushValidator;
    }

    @Override
    public MeldValidationResult validate(List<Card> cards) {
        MeldValidationResult kindResult = kindValidator.validate(cards);
        if (kindResult.valid()) {
            return kindResult;
        }

        MeldValidationResult straightFlushResult = straightFlushValidator.validate(cards);
        if (straightFlushResult.valid()) {
            return straightFlushResult;
        }

        List<MeldValidationError> errors = new ArrayList<>();
        errors.addAll(kindResult.errors());
        errors.addAll(straightFlushResult.errors());

        if (errors.isEmpty()) {
            errors.add(MeldValidationError.NO_VALID_MELD_TYPE);
        }

        return MeldValidationResult.invalid(errors.stream().distinct().toList());
    }
}
