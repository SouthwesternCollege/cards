package quetzal.cards;

import java.util.List;

public abstract class CardCollection {
    private List<Card> cards;

    public CardCollection(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return cards;
    }

    public abstract void addCard(Card card);

    public abstract Card removeCard(Card card);

}

