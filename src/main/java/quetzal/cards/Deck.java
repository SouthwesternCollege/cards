package quetzal.cards;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Deck extends CardCollection {

    private int nextCardId = 1;

    public Deck() {
        this(1, 0);
    }

    public Deck(int numberOfStandardDecks, int jokersPerDeck) {
        super(new Stack<>());

        if (numberOfStandardDecks <= 0) {
            throw new IllegalArgumentException("Number of standard decks must be positive.");
        }

        if (jokersPerDeck < 0) {
            throw new IllegalArgumentException("Jokers per deck cannot be negative.");
        }

        for (int i = 0; i < numberOfStandardDecks; i++) {
            addStandard52Cards();
            addJokers(jokersPerDeck);
        }
    }

    private void addStandard52Cards() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                addCard(Card.standard(new CardId(nextCardId++), rank, suit));
            }
        }
    }

    private void addJokers(int count) {
        for (int i = 0; i < count; i++) {
            addCard(Card.joker(new CardId(nextCardId++)));
        }
    }

    @Override
    public void addCard(Card card) {
        getCards().add(card);
    }

    @Override
    public Card removeCard(Card card) {
        getCards().remove(card);
        return card;
    }

    public Card drawCard() {
        return getCards().removeLast();
    }

    public void shuffle() {
        List<Card> cards = getCards();
        Collections.shuffle(cards, new Random());
    }
}
