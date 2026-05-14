package quetzal.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck extends CardCollection {

    public Deck() {
        this(1, 0);
    }

    public Deck(int standardDeckCount, int jokersPerDeck) {
        super(new ArrayList<>());

        if (standardDeckCount < 1) {
            throw new IllegalArgumentException("standardDeckCount must be at least 1");
        }
        if (jokersPerDeck < 0) {
            throw new IllegalArgumentException("jokersPerDeck cannot be negative");
        }

        for (int deckNumber = 0; deckNumber < standardDeckCount; deckNumber++) {
            addStandardDeck(deckNumber);
            addJokers(deckNumber, jokersPerDeck);
        }
    }

    public static Deck standard52() {
        return new Deck(1, 0);
    }

    public static Deck standard52WithJokers(int jokerCount) {
        return new Deck(1, jokerCount);
    }

    public static Deck multipleStandardDecks(int deckCount, int jokersPerDeck) {
        return new Deck(deckCount, jokersPerDeck);
    }

    private void addStandardDeck(int deckNumber) {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                addCard(new Card(rank, suit, deckNumber));
            }
        }
    }

    private void addJokers(int deckNumber, int jokerCount) {
        for (int i = 0; i < jokerCount; i++) {
            addCard(Card.joker(deckNumber));
        }
    }

    @Override
    public void addCard(Card card) {
        getCards().add(card);
    }

    @Override
    public Card removeCard(Card card) {
        if (!getCards().remove(card)) {
            return null;
        }

        return card;
    }

    public Card drawCard() {
        if (getCards().isEmpty()) {
            throw new IllegalStateException("Cannot draw from an empty deck");
        }

        return getCards().removeLast();
    }

    public void shuffle() {
        shuffle(new Random());
    }

    public void shuffle(Random random) {
        List<Card> cards = getCards();
        Collections.shuffle(cards, random);
    }
}
