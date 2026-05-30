package quetzal.cards;

import java.util.List;

/**
 * Presentation adapter that translates selected cards into HUD text.
 *
 * This keeps CardAnimationComponent from knowing about MeldValidator or GameHUD.
 */
public final class HudMeldSelectionFeedback implements SelectionFeedback {

    private final MeldValidator meldValidator;
    private final GameHudController hudController;

    public HudMeldSelectionFeedback(MeldValidator meldValidator, GameHudController hudController) {
        if (meldValidator == null) {
            throw new IllegalArgumentException("Meld validator cannot be null.");
        }

        if (hudController == null) {
            throw new IllegalArgumentException("HUD controller cannot be null.");
        }

        this.meldValidator = meldValidator;
        this.hudController = hudController;
    }

    @Override
    public void selectionChanged(List<Card> selectedCards) {
        if (selectedCards.isEmpty()) {
            hudController.setSelectedMeld("");
            return;
        }

        MeldValidationResult result = meldValidator.validate(selectedCards);
        hudController.setSelectedMeld(result.displayText());
    }

    @Override
    public void invalidPlayAttempt(MeldValidationResult validationResult) {
        hudController.setSelectedMeld(validationResult.displayText());
    }
}
