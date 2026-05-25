package quetzal.cards;

import java.util.List;

/**
 * Presentation adapter that translates selected cards into HUD text.
 *
 * This keeps CardAnimationComponent from knowing about MeldValidator or GameHUD.
 * The static GameHUD call is still transitional and should disappear when the HUD
 * becomes event/property-driven.
 */
public final class HudMeldSelectionFeedback implements SelectionFeedback {

    private final MeldValidator meldValidator;

    public HudMeldSelectionFeedback(MeldValidator meldValidator) {
        this.meldValidator = meldValidator;
    }

    @Override
    public void selectionChanged(List<Card> selectedCards) {
        if (selectedCards.isEmpty()) {
            GameHUD.updateHandRank("");
            return;
        }

        MeldValidationResult result = meldValidator.validate(selectedCards);
        GameHUD.updateHandRank(result.displayText());
    }

    @Override
    public void invalidPlayAttempt(MeldValidationResult validationResult) {
        GameHUD.updateHandRank(validationResult.displayText());
    }
}
