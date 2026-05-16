package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public class CardComponent extends Component {

    private static final int FRAME_WIDTH = 71;
    private static final int FRAME_HEIGHT = 95;
    private static final double CARD_SCALE = 2.0;

    private final Card card;

    private Texture baseTexture;
    private Texture overlayTexture;
    private Texture jokerTexture;

    public CardComponent(Card card) {
        this.card = card;

        if (card.isJoker()) {
            jokerTexture = scaledTexture(FXGL.texture("joker.png"));
        } else {
            baseTexture = scaledTexture(loadCardBaseTexture());
            overlayTexture = scaledTexture(loadStandardCardOverlayTexture(card));
        }
    }

    protected Card getCard() {
        return card;
    }

    private Texture loadCardBaseTexture() {
        return FXGL.texture("card-backs-enhancers-seals.png")
                .subTexture(new Rectangle2D(FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
    }

    private Texture loadStandardCardOverlayTexture(Card card) {
        Image image = FXGL.image("deck.png");
        int framesPerRow = (int) (image.getWidth() / FRAME_WIDTH);
        int spriteIndex = spriteIndexForStandardCard(card);

        int xIndex = spriteIndex % framesPerRow;
        int yIndex = spriteIndex / framesPerRow;

        return FXGL.texture("deck.png")
                .subTexture(new Rectangle2D(
                        xIndex * FRAME_WIDTH,
                        yIndex * FRAME_HEIGHT,
                        FRAME_WIDTH,
                        FRAME_HEIGHT
                ));
    }

    private int spriteIndexForStandardCard(Card card) {
        return card.suit().ordinal() * Rank.values().length + card.rank().ordinal();
    }

    private Texture scaledTexture(Texture texture) {
        texture.setScaleX(CARD_SCALE);
        texture.setScaleY(CARD_SCALE);
        return texture;
    }

    @Override
    public void onAdded() {
        if (card.isJoker()) {
            entity.getViewComponent().addChild(jokerTexture);
            return;
        }

        entity.getViewComponent().addChild(baseTexture);
        entity.getViewComponent().addChild(overlayTexture);
    }
}
