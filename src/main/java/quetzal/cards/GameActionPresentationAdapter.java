package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.util.Duration;

/**
 * Presentation adapter for GameController action results.
 *
 * GameController owns validation and GameState mutation.
 * This adapter owns the translation from GameEvent objects to UI updates and
 * animations.
 */
public final class GameActionPresentationAdapter {

    private final GameController gameController;
    private final Hand hand;
    private final GameHudController gameHudController;
    private final DeckDiscardPanel deckDiscardPanel;
    private final DebugHandOverlay debugHandOverlay;
    private final PassDeviceOverlay passDeviceOverlay;

    private PlayerId renderedPlayerId;

    public GameActionPresentationAdapter(
            GameController gameController,
            Hand hand,
            GameHudController gameHudController,
            DeckDiscardPanel deckDiscardPanel,
            DebugHandOverlay debugHandOverlay,
            PassDeviceOverlay passDeviceOverlay
    ) {
        if (gameController == null) {
            throw new IllegalArgumentException("Game controller cannot be null.");
        }

        if (hand == null) {
            throw new IllegalArgumentException("Hand cannot be null.");
        }

        if (gameHudController == null) {
            throw new IllegalArgumentException("Game HUD controller cannot be null.");
        }

        if (deckDiscardPanel == null) {
            throw new IllegalArgumentException("Deck/discard panel cannot be null.");
        }

        if (debugHandOverlay == null) {
            throw new IllegalArgumentException("Debug hand overlay cannot be null.");
        }

        if (passDeviceOverlay == null) {
            throw new IllegalArgumentException("Pass-device overlay cannot be null.");
        }

        this.gameController = gameController;
        this.hand = hand;
        this.gameHudController = gameHudController;
        this.deckDiscardPanel = deckDiscardPanel;
        this.debugHandOverlay = debugHandOverlay;
        this.passDeviceOverlay = passDeviceOverlay;
        this.renderedPlayerId = gameController.state().roundState().activePlayerId();
    }

    public PlayerId renderedPlayerId() {
        return renderedPlayerId;
    }

    public void handleActionResult(ActionResult result, Point2D sourcePosition) {
        if (result == null) {
            return;
        }

        if (!result.success()) {
            System.out.println("Action failed: " + result.message());
            return;
        }

        for (GameEvent event : result.events()) {
            handleGameEvent(event, sourcePosition);
        }

        refreshViews();
    }

    private void handleGameEvent(GameEvent event, Point2D sourcePosition) {
        if (event instanceof CardDrawnEvent cardDrawnEvent) {
            handleCardDrawn(cardDrawnEvent, sourcePosition);
            return;
        }

        if (event instanceof CardDiscardedEvent cardDiscardedEvent) {
            handleCardDiscarded(cardDiscardedEvent, sourcePosition);
            return;
        }

        if (event instanceof MeldCreatedEvent meldCreatedEvent) {
            handleMeldCreated(meldCreatedEvent);
            return;
        }

        if (event instanceof ActivePlayerChangedEvent activePlayerChangedEvent) {
            handleActivePlayerChanged(activePlayerChangedEvent);
            return;
        }

        if (event instanceof HandOrderChangedEvent handOrderChangedEvent) {
            handleHandOrderChanged(handOrderChangedEvent);
            return;
        }

        if (event instanceof CustomHandOrderSavedEvent customHandOrderSavedEvent) {
            handleCustomHandOrderSaved(customHandOrderSavedEvent);
        }
    }

    private void handleCardDrawn(CardDrawnEvent event, Point2D sourcePosition) {
        if (event.playerId().equals(renderedPlayerId)) {
            hand.addCardFromSource(event.card(), sourcePosition);
        }
    }

    private void handleCardDiscarded(CardDiscardedEvent event, Point2D discardPosition) {
        if (event.playerId().equals(renderedPlayerId)) {
            hand.discardSelectedCardVisual(event.card(), discardPosition);
        }

        deckDiscardPanel.setTopDiscardCard(event.card());
    }

    private void handleMeldCreated(MeldCreatedEvent event) {
        if (event.playerId().equals(renderedPlayerId)) {
            hand.displayCreatedMeld(event.playerId(), event.meld().cards());
        }
    }

    private void handleActivePlayerChanged(ActivePlayerChangedEvent event) {
        FXGL.runOnce(
                () -> showPassDeviceScreen(event.newPlayerId()),
                Duration.seconds(AnimationSettings.PLAYED_CARD_MOVE_SECONDS + 0.05)
        );
    }

    private void showPassDeviceScreen(PlayerId nextPlayerId) {
        hand.clearVisibleHand();
        refreshViews();

        passDeviceOverlay.show(
                nextPlayerId,
                () -> renderActivePlayerHand(nextPlayerId)
        );
    }

    private void handleHandOrderChanged(HandOrderChangedEvent event) {
        if (event.playerId().equals(renderedPlayerId)) {
            hand.applyHandOrder(event.orderedCards());
        }
    }

    private void handleCustomHandOrderSaved(CustomHandOrderSavedEvent event) {
        if (event.playerId().equals(renderedPlayerId)) {
            hand.playCustomOrderSavedFeedback();
        }
    }

    private void renderActivePlayerHand(PlayerId playerId) {
        renderedPlayerId = playerId;
        hand.renderHand(gameController.handFor(playerId));
        refreshViews();
    }

    private void refreshViews() {
        gameHudController.refreshFromGameState(gameController.state());
        deckDiscardPanel.refresh();
        debugHandOverlay.refresh();
    }
}
