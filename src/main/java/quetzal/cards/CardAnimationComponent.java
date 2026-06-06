package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class CardAnimationComponent extends Component {

    private Point2D initialPosition;  // Initial position of the dragged card
    private Point2D initialMousePosition; // Initial mouse position when clicked
    private boolean isDragging = false;  // Track if a card is being dragged
    private int lastDragIndex = -1;

    // Offset to ensure the card stays centered on the mouse
    private double offsetX;
    private double offsetY;

    // Transitional dependency: this component still delegates interaction to Hand.
    // It no longer validates melds or updates the HUD directly.
    private final Hand hand;
    private double cardSpacing; // Space between cards
    private boolean interactionEnabled = true;
    private boolean hovered = false;

    public CardAnimationComponent(Hand hand) {
        this.hand = hand;
    }

    @Override
    public void onAdded() {
        refreshHoverAnimationState();

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            hovered = true;

            if (interactionEnabled) {
                setHovered(true);
            }
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            hovered = false;

            if (interactionEnabled) {
                setHovered(false);
            }
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
    }

    public void setInteractionEnabled(boolean interactionEnabled) {
        this.interactionEnabled = interactionEnabled;

        if (!interactionEnabled) {
            isDragging = false;
            hovered = false;
            setHovered(false);
            setWiggleEnabled(false);
            entity.getComponent(CardComponent.class).resetVisualRotation();
            entity.setRotation(0.0);
        } else {
            refreshHoverAnimationState();
        }
    }

    private void setHovered(boolean hovered) {
        entity.getComponent(CardWiggleComponent.class).setHovered(hovered);
    }

    private void setWiggleEnabled(boolean enabled) {
        entity.getComponent(CardWiggleComponent.class).setEnabled(enabled);
    }

    private void refreshHoverAnimationState() {
        if (!interactionEnabled) {
            return;
        }

        setWiggleEnabled(true);
        setHovered(hovered);
    }

    private void raiseCard() {
        Card card = entity.getComponent(CardComponent.class).getCard();
        int index = hand.getCards().indexOf(card);

        if (index < 0) {
            return;
        }

        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))
                .interpolator(Interpolators.CIRCULAR.EASE_OUT())
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
                .duration(Duration.seconds(0.2))
                .interpolator(Interpolators.SMOOTH.EASE_OUT())
                .translate(entity)
                .to(hand.getCardPosition(index))
                .buildAndPlay();
    }

    private void toggleCardState() {
        Card card = entity.getComponent(CardComponent.class).getCard();
        SelectionChange change = hand.toggleSelected(card);

        if (change == SelectionChange.SELECTED) {
            raiseCard();
        } else if (change == SelectionChange.DESELECTED) {
            lowerCard();
        }

        refreshHoverAnimationState();
    }

    private void onMousePressed(MouseEvent event) {
        if (!interactionEnabled) {
            return;
        }

        cardSpacing = hand.getCardSpacing();
        if (event.getButton() == MouseButton.PRIMARY) {
            initialPosition = entity.getPosition();
            initialMousePosition = new Point2D(event.getSceneX(), event.getSceneY());

            offsetX = event.getSceneX() - entity.getX();
            offsetY = event.getSceneY() - entity.getY();

            setWiggleEnabled(false);
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (!interactionEnabled) {
            return;
        }

        double distanceMoved = initialMousePosition.distance(event.getSceneX(), event.getSceneY());

        final double DRAG_THRESHOLD = 1;

        if (!isDragging && distanceMoved > DRAG_THRESHOLD) {
            isDragging = true;

            Card draggedCard = entity.getComponent(CardComponent.class).getCard();
            lastDragIndex = hand.getCards().indexOf(draggedCard);

            entity.getViewComponent().setZIndex(100);
            entity.setZIndex(100);
        }

        if (isDragging) {
            entity.setPosition(event.getSceneX() - offsetX, event.getSceneY() - offsetY);
            reorganizeHandDuringDrag();
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (!interactionEnabled) {
            return;
        }

        if (isDragging) {
            hand.sortCardsByPosition(hand.getCards());
            hand.organizeCardEntities();

            lastDragIndex = -1;
            isDragging = false;
        } else {
            if (hand.isSelectable(entity.getComponent(CardComponent.class).getCard())) {
                toggleCardState();
            }
        }

        refreshHoverAnimationState();
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
}
