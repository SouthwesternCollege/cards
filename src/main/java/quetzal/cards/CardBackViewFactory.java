package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Creates card-back views from the card-backs/enhancers/seals sprite sheet.
 *
 * Current rule:
 * - Use the upper-left sprite as the standard card back.
 */
public final class CardBackViewFactory {

    private static final String CARD_BACK_SHEET = "card-backs-enhancers-seals.png";

    public Node createCardBackView() {
        Image image = FXGL.image(CARD_BACK_SHEET);

        ImageView view = new ImageView(image);
        view.setViewport(new Rectangle2D(
                0,
                0,
                CardViewMetrics.spriteWidth(),
                CardViewMetrics.spriteHeight()
        ));
        view.setFitWidth(CardViewMetrics.renderedWidth());
        view.setFitHeight(CardViewMetrics.renderedHeight());
        view.setPreserveRatio(false);
        view.setSmooth(false);

        return view;
    }
}
