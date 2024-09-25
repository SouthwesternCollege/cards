package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class CardWiggleComponent extends Component {

    private boolean isRaised = false;  // New flag to track if the card is raised or not
    private double defaultWiggleAmplitude = 2;  // Default wiggle amplitude
    private double hoverWiggleAmplitude = 5;   // Increased wiggle amplitude on hover

    @Override
    public void onAdded() {

        // Start the wiggle animation when the entity is added
        startWiggle(defaultWiggleAmplitude);

        // Add a mouse click listener to the entity
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {

                // Toggle between raised and lowered states on click
                toggleCardState();
            }
        });

        // Add mouse hover event listeners
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            // Increase the wiggle amplitude on hover
            increaseWiggle();
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            // Revert to default wiggle amplitude when not hovering
            revertWiggle();
        });
    }

    private void increaseWiggle() {
            startWiggle(hoverWiggleAmplitude);

    }

    private void revertWiggle() {
            startWiggle(defaultWiggleAmplitude);

    }

    private void startWiggle(double amplitude) {
        // Start a rotation animation with a given amplitude
        FXGL.animationBuilder()
                .duration(Duration.seconds(2))    // Duration of one wiggle cycle
                .repeatInfinitely()               // Repeat the wiggle indefinitely
                .autoReverse(true)                // Wiggle back and forth
                .interpolator(Interpolators.SMOOTH.EASE_OUT())  // Elastic effect for smoothness
                .rotate(entity)
                .origin(new Point2D(35,45))
                .from(-amplitude)
                .to(amplitude)
                .buildAndPlay();
    }


    private void raiseCard() {
        // Create an animation to move the card upwards
        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))  // Duration of the raise animation
                .interpolator(Interpolators.CIRCULAR.EASE_OUT())  // Smooth bounce effect
                .translate(entity)
                .from(entity.getPosition())       // Start from the current position
                .to(entity.getPosition().add(0, -20))  // Move the card up by 50 pixels
                .buildAndPlay();
    }

    private void lowerCard() {
        // Create an animation to move the card back to the original position
        FXGL.animationBuilder()
                .duration(Duration.seconds(0.2))  // Duration of the lower animation
                .interpolator(Interpolators.SMOOTH.EASE_OUT())  // Smooth bounce effect
                .translate(entity)
                .from(entity.getPosition())       // Start from the current position
                .to(entity.getPosition().add(0, 20))  // Move the card down by 50 pixels
                .buildAndPlay();
    }

    private void toggleCardState() {
        if (isRaised) {
            // If the card is raised, lower it
            lowerCard();
        } else {
            // If the card is not raised, raise it
            raiseCard();
        }
        // Toggle the state
        isRaised = !isRaised;
    }
    public void remove() {
        entity.removeFromWorld();
    }
}