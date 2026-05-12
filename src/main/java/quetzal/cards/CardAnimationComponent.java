package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.List;

public class CardAnimationComponent extends Component {

    private final double DEFAULT_WIGGLE_AMPLITUDE = 2;  // Default wiggle amplitude

    private Point2D initialPosition;  // Initial position of the dragged card
    private Point2D initialMousePosition; // Initial mouse position when clicked
    private boolean isDragging = false;  // Track if a card is being dragged
    private boolean isRaised = false;  // New flag to track if the card is raised or not
    private int lastDragIndex = -1;

    // Offset to ensure the card stays centered on the mouse
    private double offsetX;
    private double offsetY;

    // I think that all of this information should be kept out of the CardAnimationComponent class
    private static Text handRankText; // Hand-rank Text component
    private Hand hand; // List of all cards in the game
    private double cardSpacing; // Space between cards

    @Override
    public void onAdded() {

        // Start the wiggle animation when the entity is added
        startWiggle(DEFAULT_WIGGLE_AMPLITUDE, 2);

        // Add mouse hover event listeners
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            // Increase the wiggle amplitude on hover
            increaseWiggle();
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            // Revert to default wiggle amplitude when not hovering
            revertWiggle();
        });


        // Add mouse click and drag event handlers
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
    }

    public CardAnimationComponent(Hand hand) {
        this.hand = hand;
    }

    private void increaseWiggle() {
        // Increased wiggle amplitude on hover
        double HOVER_WIGGLE_AMPLITUDE = 4;
        // Increased wiggle amplitude on hover
        double HOVER_WIGGLE_DURATION = 1;
        startWiggle(HOVER_WIGGLE_AMPLITUDE, HOVER_WIGGLE_DURATION);

    }

    private void revertWiggle() {
        // Default wiggle amplitude
        double DEFAULT_WIGGLE_DURATION = 2;
        startWiggle(DEFAULT_WIGGLE_AMPLITUDE, DEFAULT_WIGGLE_DURATION);

    }

    private void startWiggle(double amplitude, double duration) {
        // Start a rotation animation with a given amplitude
        FXGL.animationBuilder()
                .duration(Duration.seconds(duration))    // Duration of one wiggle cycle
                .repeatInfinitely()               // Repeat the wiggle indefinitely
                .autoReverse(true)                // Wiggle back and forth
                .interpolator(Interpolators.SMOOTH.EASE_OUT())  // Elastic effect for smoothness
                .rotate(entity)
                .origin(new Point2D(35, 45))
                .from(-amplitude)
                .to(amplitude)
                .buildAndPlay();
    }

    private void raiseCard() {
        Card card = entity.getComponent(CardComponent.class).getCard();
        int index = hand.getCards().indexOf(card);

        if (index < 0) {
            return;
        }

        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))  // Duration of the raise animation
                .interpolator(Interpolators.CIRCULAR.EASE_OUT())  // Smooth bounce effect
                .translate(entity)
                .to(hand.getCardVisualPosition(index))
                .buildAndPlay();
    }

    private void lowerCard() {
        Card card = entity.getComponent(CardComponent.class).getCard();
        int index = hand.getCards().indexOf(card);

        if (index < 0) {
            return;
        }

        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))  // Duration of the lower animation
                .interpolator(Interpolators.SMOOTH.EASE_OUT())  // Smooth effect
                .translate(entity)
                .to(hand.getCardPosition(index))
                .buildAndPlay();
    }

    private void updateHandRankText(String rank) {
        if (handRankText == null) {
            handRankText = new Text();
            handRankText.setFont(Font.loadFont(getClass().getResourceAsStream("/DePixelHalbfett.ttf"), 24));
            handRankText.setFill(Color.WHITE);

            // Temporary position. Later this can move into your HUD area.
            handRankText.setTranslateX(100);
            handRankText.setTranslateY(50);

            FXGL.getGameScene().addUINode(handRankText);
        }

        handRankText.setText(rank);
    }

    private void toggleCardState() {
        Card card = entity.getComponent(CardComponent.class).getCard();

        if (isRaised) {
            if (hand.removeSelected(card)) {
                lowerCard();
                isRaised = false;
                refreshHandRank();
            }

            return;
        }

        if (hand.addSelected(card)) {
            raiseCard();
            isRaised = true;
            refreshHandRank();
        }
    }

    private void onMousePressed(MouseEvent event) {
        cardSpacing = hand.getCardSpacing();
        if (event.getButton() == MouseButton.PRIMARY) {
            // Store the initial position of the card when the drag starts
            initialPosition = entity.getPosition();

            // Store the initial mouse position
            initialMousePosition = new Point2D(event.getSceneX(), event.getSceneY());

            // Calculate the offset to keep the card centered on the mouse
            offsetX = event.getSceneX() - entity.getX();
            offsetY = event.getSceneY() - entity.getY();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        // Calculate the distance the mouse has moved from its initial position
        double distanceMoved = initialMousePosition.distance(event.getSceneX(), event.getSceneY());

        // Threshold distance to start dragging
        final double DRAG_THRESHOLD = 1;

        // Start dragging only if the mouse has moved beyond the threshold
        if (!isDragging && distanceMoved > DRAG_THRESHOLD) {
            isDragging = true;

            Card draggedCard = entity.getComponent(CardComponent.class).getCard();
            lastDragIndex = hand.getCards().indexOf(draggedCard);

            // Bring the card to the front by increasing its zIndex
            entity.getViewComponent().setZIndex(100);
            entity.setZIndex(100);
        }

        if (isDragging) {
            // Move only the dragged card directly with the mouse.
            entity.setPosition(event.getSceneX() - offsetX, event.getSceneY() - offsetY);

            reorganizeHandDuringDrag();
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (isDragging) {
            hand.sortCardsByPosition(hand.getCards());
            hand.organizeCardEntities();

            lastDragIndex = -1;
            isDragging = false;
        } else {
            if (entity.getComponent(CardComponent.class).getCard().isSelectable()) {
                toggleCardState();
            }
        }
    }

    private void reorganizeHandDuringDrag() {
        Card draggedCard = entity.getComponent(CardComponent.class).getCard();

        hand.sortCardsByPosition(hand.getCards());

        int currentDragIndex = hand.getCards().indexOf(draggedCard);

        if (currentDragIndex != lastDragIndex) {
            lastDragIndex = currentDragIndex;
            hand.organizeCardEntitiesExcept(draggedCard);
        }
    }

    protected void organizeCards() {
        hand.sortCardsByPosition(hand.getCards());
        hand.organizeCardEntities();
    }

    private void snapCardToNearestPosition() {

        List<Card> sortedCards = hand.getCards();
        int nearestIndex = sortedCards.indexOf(entity.getComponent(CardComponent.class).getCard());

        nearestIndex = Math.clamp(nearestIndex, 0, hand.size() - 1);
        entity.setZIndex(nearestIndex);

        Point2D snapPosition = hand.getCardPosition(nearestIndex);
        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))
                .translate(entity)
                .to(snapPosition)
                .buildAndPlay();
    }

    private void refreshHandRank() {
        if (hand.getSelectedCards().isEmpty()) {
            updateHandRankText("");
            return;
        }

        int[] selectedRank = new int[hand.getSelectedCards().size()];
        for (int i = 0; i < hand.getSelectedCards().size(); i++) {
            selectedRank[i] = hand.getSelectedCards().get(i).getCardIndex();
        }

        updateHandRankText(PokerHandEvaluator.rankHand(selectedRank));
    }
}
