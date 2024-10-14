package quetzal.cards;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Deck extends CardCollection{
    protected Deck() {
        super(new Stack<>());
        for (int i = 0; i < 52; i++) {
            addCard(new Card(i));
        }
    }

    @Override
    public void addCard(Card card) {
        getCards().add(card);
    }

    @Override
    public Card removeCard(Card card) {
        return drawCard();
    }

    public Card drawCard() {
        return getCards().removeLast();
    }


    public void shuffle() {
        List<Card> cards = getCards();
        Random random = new Random();
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1); // Get a random index from 0 to i
            Collections.swap(cards, i, j); // Swap the current card with the card at the random index
        }
    }
}
