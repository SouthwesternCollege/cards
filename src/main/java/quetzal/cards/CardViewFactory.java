package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Creates the JavaFX visual node for a card at its final rendered size.
 *
 * Important invariant:
 *
 * Entity position represents the top-left corner of the rendered card.
 *
 * This class avoids transform-scaling child textures because that can make
 * layout bounds and visual bounds disagree. Instead, cropped ImageViews are
 * fitted directly to CardViewMetrics.renderedWidth/Height.
 */
public final class CardViewFactory {

    private static final String STANDARD_DECK_SHEET = "deck.png";
    private static final String CARD_BASE_SHEET = "card-backs-enhancers-seals.png";
    private static final String JOKER_IMAGE = "joker.png";

    public Node createView(Card card) {
        if (card.isJoker()) {
            return fittedImageView(FXGL.image(JOKER_IMAGE), null);
        }

        Group group = new Group();
        group.getChildren().add(createCardBaseView());
        group.getChildren().add(createStandardCardOverlayView(card));
        return group;
    }

    private Node createCardBaseView() {
        Image image = FXGL.image(CARD_BASE_SHEET);

        return fittedImageView(
                image,
                new Rectangle2D(
                        CardViewMetrics.spriteWidth(),
                        0,
                        CardViewMetrics.spriteWidth(),
                        CardViewMetrics.spriteHeight()
                )
        );
    }

    private Node createStandardCardOverlayView(Card card) {
        Image image = FXGL.image(STANDARD_DECK_SHEET);

        CardViewMetrics.configureFromStandardDeckSheet(image, CardViewMetrics.renderScale());

        int spriteIndex = spriteIndexForStandardCard(card);
        int xIndex = spriteIndex % CardViewMetrics.STANDARD_DECK_COLUMNS;
        int yIndex = spriteIndex / CardViewMetrics.STANDARD_DECK_COLUMNS;

        return fittedImageView(
                image,
                new Rectangle2D(
                        xIndex * CardViewMetrics.spriteWidth(),
                        yIndex * CardViewMetrics.spriteHeight(),
                        CardViewMetrics.spriteWidth(),
                        CardViewMetrics.spriteHeight()
                )
        );
    }

    private int spriteIndexForStandardCard(Card card) {
        return card.suit().ordinal() * Rank.values().length + card.rank().ordinal();
    }

    private ImageView fittedImageView(Image image, Rectangle2D viewport) {
        ImageView view = new ImageView(image);

        if (viewport != null) {
            view.setViewport(viewport);
        }

        view.setFitWidth(CardViewMetrics.renderedWidth());
        view.setFitHeight(CardViewMetrics.renderedHeight());
        view.setPreserveRatio(false);
        view.setSmooth(false);
        view.setX(0);
        view.setY(0);

        return view;
    }
}
