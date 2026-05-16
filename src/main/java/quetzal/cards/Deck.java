package quetzal.cards;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Deck extends CardCollection {

    private static final int DEFAULT_JOKERS_PER_STANDARD_DECK = 2;

    private final Random random;
    private int nextCardId = 1;

    public Deck() {
        this(1, 0);
    }

    public Deck(int numberOfStandardDecks, int jokersPerDeck) {
        this(numberOfStandardDecks, jokersPerDeck, new Random());
    }

    public Deck(int numberOfStandardDecks, int jokersPerDeck, Random random) {
        super(new Stack<>());

        if (numberOfStandardDecks <= 0) {
            throw new IllegalArgumentException("Number of standard decks must be positive.");
        }

        if (jokersPerDeck < 0) {
            throw new IllegalArgumentException("Jokers per deck cannot be negative.");
        }

        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null.");
        }

        this.random = random;

        addStandardDecks(numberOfStandardDecks, jokersPerDeck);
    }

    public static Deck standard52() {
        return new Deck(1, 0);
    }

    public static Deck standardDecks(int numberOfStandardDecks) {
        return new Deck(numberOfStandardDecks, 0);
    }

    public static Deck standardDecksWithJokers(int numberOfStandardDecks, int jokersPerDeck) {
        return new Deck(numberOfStandardDecks, jokersPerDeck);
    }

    public static Deck laKikaPrototypeDeck() {
        return new Deck(2, DEFAULT_JOKERS_PER_STANDARD_DECK);
    }

    public void addShuffledStandardDeckWithJokers() {
        addShuffledStandardDeckWithJokers(DEFAULT_JOKERS_PER_STANDARD_DECK);
    }

    public void addShuffledStandardDeckWithJokers(int jokersPerDeck) {
        if (jokersPerDeck < 0) {
            throw new IllegalArgumentException("Jokers per deck cannot be negative.");
        }

        addStandard52Cards();
        addJokers(jokersPerDeck);
        shuffle();
    }

    private void addStandardDecks(int numberOfStandardDecks, int jokersPerDeck) {
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
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }

        getCards().add(card);
    }

    @Override
    public Card removeCard(Card card) {
        if (!getCards().remove(card)) {
            throw new IllegalArgumentException("Card is not in deck: " + card);
        }

        return card;
    }

    public Card drawCard() {
        if (getCards().isEmpty()) {
            throw new IllegalStateException("Cannot draw from an empty deck.");
        }

        return getCards().removeLast();
    }

    public void shuffle() {
        List<Card> cards = getCards();
        Collections.shuffle(cards, random);
    }
}
