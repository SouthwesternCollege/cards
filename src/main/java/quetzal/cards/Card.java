package quetzal.cards;

public class Card {

    private static final int RANKS_PER_SUIT = 13;

    private final Rank rank;
    private final Suit suit;
    private final int deckNumber;
    private final boolean joker;
    private boolean selectable = true;

    public Card(int cardIndex) {
        if (cardIndex < 0 || cardIndex >= 52) {
            throw new IllegalArgumentException("Standard card index must be between 0 and 51: " + cardIndex);
        }

        this.rank = Rank.fromSpriteColumn(cardIndex % RANKS_PER_SUIT);
        this.suit = Suit.fromSpriteRow(cardIndex / RANKS_PER_SUIT);
        this.deckNumber = 0;
        this.joker = false;
    }

    public Card(Rank rank, Suit suit) {
        this(rank, suit, 0);
    }

    public Card(Rank rank, Suit suit, int deckNumber) {
        if (rank == null) {
            throw new IllegalArgumentException("rank cannot be null");
        }
        if (suit == null) {
            throw new IllegalArgumentException("suit cannot be null");
        }

        this.rank = rank;
        this.suit = suit;
        this.deckNumber = deckNumber;
        this.joker = false;
    }

    public static Card joker(int deckNumber) {
        return new Card(deckNumber);
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getDeckNumber() {
        return deckNumber;
    }

    public boolean isJoker() {
        return joker;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public int getCardIndex() {
        if (joker) {
            return 52;
        }

        return suit.spriteRow() * RANKS_PER_SUIT + rank.spriteColumn();
    }

    public int rank() {
        if (joker) {
            return Integer.MAX_VALUE;
        }

        return rank.pokerValue();
    }

    public Suit suit() {
        return suit;
    }

    @Override
    public String toString() {
        if (joker) {
            return "Joker (deck " + deckNumber + ")";
        }

        return rank.displayName() + " of " + suit.pluralDisplayName() + " (deck " + deckNumber + ")";
    }
}
