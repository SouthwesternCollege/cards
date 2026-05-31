package quetzal.cards;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.Node;

public class CardComponent extends Component {

    private static final CardViewFactory CARD_VIEW_FACTORY = new CardViewFactory();

    private final Card card;

    public CardComponent(Card card) {
        this.card = card;
    }

    protected Card getCard() {
        return card;
    }

    @Override
    public void onAdded() {
        Node cardView = CARD_VIEW_FACTORY.createView(card);
        entity.getViewComponent().addChild(cardView);
    }
}
