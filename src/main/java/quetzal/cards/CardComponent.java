package quetzal.cards;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public class CardComponent extends Component {

    private Texture baseTexture;
    private Texture overlayTexture;
    private int rank;


    public CardComponent(int frameIndex) {
        this.rank = frameIndex;
        Image image = FXGL.image("deck.png");
        int frameWidth = 71;
        int frameHeight = 95;

        int framesPerRow = (int) (image.getWidth() / frameWidth);

        int xIndex = frameIndex % framesPerRow;
        int yIndex = frameIndex / framesPerRow;

        // Load the sprite-sheet and select tile
        // Base
        overlayTexture = FXGL.texture("deck.png")
                .subTexture(new Rectangle2D(xIndex * frameWidth, yIndex * frameHeight,frameWidth, frameHeight));
        overlayTexture.setScaleX(2.0);
        overlayTexture.setScaleY(2.0);
        // Overlay
        baseTexture = FXGL.texture("card-backs-enhancers-seals.png")
                .subTexture(new Rectangle2D(frameWidth, 0, frameWidth, frameHeight));
        baseTexture.setScaleX(2.0);
        baseTexture.setScaleY(2.0);

    }

    public int getRank() {
        return rank;
    }

    @Override
    public void onAdded() {
        // Add the static texture to the entity's view
        entity.getViewComponent().addChild(baseTexture);
        entity.getViewComponent().addChild(overlayTexture);
    }

}
