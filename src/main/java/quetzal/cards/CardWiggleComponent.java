package quetzal.cards;

import com.almasb.fxgl.entity.component.Component;

/**
 * Stateful card wiggle / hover emphasis animation.
 *
 * The phase is continuous. Hover changes target amplitude/speed/scale instead of
 * restarting an animation timeline, so the card no longer jumps to a new
 * starting angle.
 */
public final class CardWiggleComponent extends Component {

    // Regular wiggle is intentionally subtle.
    // Previous values were amplitude=2.0 and cycle=2.0 seconds.
    // This cuts amplitude and frequency by roughly 50%.
    private static final double DEFAULT_AMPLITUDE = 1.0;
    private static final double DEFAULT_CYCLE_SECONDS = 4.0;

    // Hover should emphasize the card without becoming frantic.
    private static final double HOVER_AMPLITUDE = 2.0;
    private static final double HOVER_CYCLE_SECONDS = 2.0;
    private static final double HOVER_SCALE = 1.05;

    private static final double TRANSITION_SPEED = 8.0;
    private static final double SCALE_TRANSITION_SPEED = 14.0;
    private static final double SCALE_BOUNCE_STRENGTH = 0.018;

    private double phase = 0.0;
    private double amplitude = DEFAULT_AMPLITUDE;
    private double targetAmplitude = DEFAULT_AMPLITUDE;
    private double angularSpeed = angularSpeed(DEFAULT_CYCLE_SECONDS);
    private double targetAngularSpeed = angularSpeed(DEFAULT_CYCLE_SECONDS);

    private double visualScale = 1.0;
    private double targetScale = 1.0;
    private boolean hovered = false;
    private boolean enabled = true;

    @Override
    public void onUpdate(double tpf) {
        if (!enabled) {
            resetCardVisuals();
            return;
        }

        double blend = Math.min(1.0, tpf * TRANSITION_SPEED);
        amplitude = lerp(amplitude, targetAmplitude, blend);
        angularSpeed = lerp(angularSpeed, targetAngularSpeed, blend);

        phase += angularSpeed * tpf;
        setCardRotation(Math.sin(phase) * amplitude);

        updateScale(tpf);
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;

        if (hovered) {
            targetAmplitude = HOVER_AMPLITUDE;
            targetAngularSpeed = angularSpeed(HOVER_CYCLE_SECONDS);
            targetScale = HOVER_SCALE;
        } else {
            targetAmplitude = DEFAULT_AMPLITUDE;
            targetAngularSpeed = angularSpeed(DEFAULT_CYCLE_SECONDS);
            targetScale = 1.0;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            hovered = false;
            targetAmplitude = 0.0;
            amplitude = 0.0;
            targetScale = 1.0;
            visualScale = 1.0;
            resetCardVisuals();
        } else {
            targetAmplitude = DEFAULT_AMPLITUDE;
            targetAngularSpeed = angularSpeed(DEFAULT_CYCLE_SECONDS);
            targetScale = hovered ? HOVER_SCALE : 1.0;
        }
    }

    private void updateScale(double tpf) {
        double blend = Math.min(1.0, tpf * SCALE_TRANSITION_SPEED);
        visualScale = lerp(visualScale, targetScale, blend);

        // Small damped pulse while hovering. This gives a light bounce feel
        // without using a separate timeline that could fight the stateful model.
        double bounce = 0.0;
        if (hovered && Math.abs(targetScale - visualScale) < 0.02) {
            bounce = Math.sin(phase * 1.35) * SCALE_BOUNCE_STRENGTH;
        }

        setCardScale(visualScale + bounce);
    }

    private void setCardRotation(double angle) {
        entity.getComponent(CardComponent.class).setVisualRotation(angle);
    }

    private void setCardScale(double scale) {
        entity.getComponent(CardComponent.class).setVisualScale(scale);
    }

    private void resetCardVisuals() {
        CardComponent cardComponent = entity.getComponent(CardComponent.class);
        cardComponent.resetVisualRotation();
        cardComponent.resetVisualScale();
        entity.setRotation(0.0);
    }

    private static double angularSpeed(double cycleSeconds) {
        return Math.PI * 2.0 / cycleSeconds;
    }

    private static double lerp(double start, double end, double amount) {
        return start + (end - start) * amount;
    }
}
