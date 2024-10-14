package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import java.util.List;

public class CardAnimationComponent extends Component {

    private final double DEFAULT_WIGGLE_AMPLITUDE = 2;  // Default wiggle amplitude
    private final double HOVER_WIGGLE_AMPLITUDE = 4;   // Increased wiggle amplitude on hover
    private final double DEFAULT_WIGGLE_DURATION = 2;  // Default wiggle amplitude
    private final double HOVER_WIGGLE_DURATION = 1;   // Increased wiggle amplitude on hover
    private final double DRAG_THRESHOLD = 1; // Threshold distance to start dragging
    private final double DEFAULT_CARD_SPACING = 160;

    private Hand hand; // List of all cards in the game
    private double cardSpacing; // Space between cards
    private Point2D initialPosition;  // Initial position of the dragged card
    private Point2D initialMousePosition; // Initial mouse position when clicked
    private boolean isDragging = false;  // Track if a card is being dragged
    private boolean isRaised = false;  // New flag to track if the card is raised or not

    // Offset to ensure the card stays centered on the mouse
    private double offsetX;
    private double offsetY;

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
        startWiggle(HOVER_WIGGLE_AMPLITUDE, HOVER_WIGGLE_DURATION);

    }

    private void revertWiggle() {
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
        // Add card to selected list, maximum selected is 5

        // Create an animation to move the card upwards
        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))  // Duration of the raise animation
                .interpolator(Interpolators.CIRCULAR.EASE_OUT())  // Smooth bounce effect
                .translate(entity)
                .from(entity.getPosition())       // Start from the current position
                .to(entity.getPosition().add(0, -50))  // Move the card up by 50px
                .buildAndPlay();
    }

    private void lowerCard() {

        // Create an animation to move the card back to the original position
        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))  // Duration of the lower animation
                .interpolator(Interpolators.SMOOTH.EASE_OUT())  // Smooth effect
                .translate(entity)
                .from(entity.getPosition())       // Start from the current position
                .to(entity.getPosition().add(0, 50))  // Move the card down by 50 pixels
                .buildAndPlay();
    }

    private void toggleCardState() {
        if (isRaised) {
            hand.removeSelected(entity.getComponent(CardComponent.class).getCard()); // Remove from selected
            lowerCard();                  // Lower the card
            isRaised = false;             // Update the state
        } else {
            if (hand.getSelectedCards().size() < 5) {
                hand.addSelected(entity.getComponent(CardComponent.class).getCard()); // Add to selected
                raiseCard();              // Raise the card
                isRaised = true;          // Update the state
                // No immediate confirmation or playing of cards here

                // Determine poker hand
                int[] selectedRank = new int[hand.getSelectedCards().size()];
                for (int i = 0; i < hand.getSelectedCards().size(); i++) {
                    selectedRank[i] = hand.getSelectedCards().get(i).getCardIndex();
                }
                System.out.println(PokerHandEvaluator.rankHand(selectedRank));
            }
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

        // Start dragging only if the mouse has moved beyond the threshold
        if (!isDragging && distanceMoved > DRAG_THRESHOLD) {
            isDragging = true;

            // Bring the card to the front by increasing its zIndex
            entity.getViewComponent().setZIndex(100);
        }

        if (isDragging) {
            // Move the card to follow the mouse cursor, adjusted by the original offset
            entity.setPosition(event.getSceneX() - offsetX, event.getSceneY() - offsetY);

            organizeCards();
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (isDragging) {

            // Reorganize the cards
            organizeCards();

            // Snap the dragged card to the nearest position when the drag is released
            snapCardToNearestPosition();

            // Reset the dragging flag
            isDragging = false;
        } else {
            if (entity.getComponent(CardComponent.class).getCard().isSelectable()){
                toggleCardState();
            }
        }
    }

    protected void organizeCards() {
        hand.sortCardsByPosition(hand.getCards());  // Sort cards once

        List<Card> sortedCards = hand.getCards(); // Already sorted by position
        cardSpacing = Math.min(hand.getCardSpacing(), DEFAULT_CARD_SPACING);

        for (int i = 0; i < sortedCards.size(); i++) {
            Entity cardEntity = sortedCards.get(i).getEntity();
            double targetX = i * cardSpacing + FXGL.getAppWidth() / 3;
            cardEntity.setZIndex(i);

            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.2))
                    .translate(cardEntity)
                    .to(new Point2D(targetX, cardEntity.getY()))
                    .buildAndPlay();
        }
    }

    private void snapCardToNearestPosition() {

        List<Card> sortedCards = hand.getCards();
        int nearestIndex = sortedCards.indexOf(entity.getComponent(CardComponent.class).getCard());

        nearestIndex = Math.max(0, Math.min(nearestIndex, hand.size() - 1));
        entity.setZIndex(nearestIndex);

        Point2D snapPosition = new Point2D(nearestIndex * cardSpacing + FXGL.getAppWidth() / 3, initialPosition.getY());
        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))
                .translate(entity)
                .to(snapPosition)
                .buildAndPlay();
    }


}
