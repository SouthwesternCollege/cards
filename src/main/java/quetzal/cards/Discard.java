package quetzal.cards;

import java.util.Stack;

public class Discard extends CardCollection{

    protected Discard() {
        super(new Stack<>());
    }

    @Override
    public void addCard(Card card) {

    }

    @Override
    public Card removeCard(Card card) {
        return null;
    }
}
