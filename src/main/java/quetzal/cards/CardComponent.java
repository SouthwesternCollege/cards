package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public class CardComponent extends Component {

    private static final int FRAME_WIDTH = 71;
    private static final int FRAME_HEIGHT = 95;

    private final Card card;
    private Texture baseTexture;
    private Texture overlayTexture;

    public CardComponent(Card card) {
        this.card = card;

        Image image = FXGL.image("deck.png");
        int framesPerRow = (int) (image.getWidth() / FRAME_WIDTH);
        int spriteIndex = spriteIndexFor(card);

        int xIndex = spriteIndex % framesPerRow;
        int yIndex = spriteIndex / framesPerRow;

        overlayTexture = FXGL.texture("deck.png")
                .subTexture(new Rectangle2D(xIndex * FRAME_WIDTH, yIndex * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
        overlayTexture.setScaleX(2.0);
        overlayTexture.setScaleY(2.0);

        baseTexture = FXGL.texture("card-backs-enhancers-seals.png")
                .subTexture(new Rectangle2D(FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
        baseTexture.setScaleX(2.0);
        baseTexture.setScaleY(2.0);
    }

    protected Card getCard() {
        return card;
    }

    private int spriteIndexFor(Card card) {
        if (card.isJoker()) {
            // Temporary fallback until we choose the exact joker sprite.
            return 0;
        }

        return card.suit().ordinal() * Rank.values().length + card.rank().ordinal();
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(baseTexture);
        entity.getViewComponent().addChild(overlayTexture);
    }
}
