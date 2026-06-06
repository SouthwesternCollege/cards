package quetzal.cards;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

public class CardComponent extends Component {

    private static final CardViewFactory CARD_VIEW_FACTORY = new CardViewFactory();

    private final Card card;
    private final Rotate visualRotation = new Rotate(0.0);
    private final Scale visualScale = new Scale(1.0, 1.0);

    private Group visualRoot;

    public CardComponent(Card card) {
        this.card = card;
    }

    protected Card getCard() {
        return card;
    }

    @Override
    public void onAdded() {
        Node cardView = CARD_VIEW_FACTORY.createView(card);

        double pivotX = CardViewMetrics.renderedWidth() / 2.0;
        double pivotY = CardViewMetrics.renderedHeight() / 2.0;

        visualRotation.setPivotX(pivotX);
        visualRotation.setPivotY(pivotY);

        visualScale.setPivotX(pivotX);
        visualScale.setPivotY(pivotY);

        visualRoot = new Group(cardView);
        visualRoot.getTransforms().addAll(visualScale, visualRotation);

        entity.getViewComponent().addChild(visualRoot);
    }

    public void setVisualRotation(double angle) {
        visualRotation.setAngle(angle);
    }

    public void resetVisualRotation() {
        setVisualRotation(0.0);
    }

    public void setVisualScale(double scale) {
        visualScale.setX(scale);
        visualScale.setY(scale);
    }

    public void resetVisualScale() {
        setVisualScale(1.0);
    }
}
