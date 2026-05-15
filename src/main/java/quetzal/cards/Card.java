package quetzal.cards;

public final class Card {

    private final CardId id;
    private final Rank rank;
    private final Suit suit;
    private final boolean joker;

    private boolean selectable = true;

    private Card(CardId id, Rank rank, Suit suit, boolean joker) {
        if (id == null) {
            throw new IllegalArgumentException("Card id cannot be null.");
        }

        if (!joker && (rank == null || suit == null)) {
            throw new IllegalArgumentException("A standard card must have a rank and suit.");
        }

        if (joker && (rank != null || suit != null)) {
            throw new IllegalArgumentException("A joker cannot have a rank or suit.");
        }

        this.id = id;
        this.rank = rank;
        this.suit = suit;
        this.joker = joker;
    }

    public static Card standard(CardId id, Rank rank, Suit suit) {
        return new Card(id, rank, suit, false);
    }

    public static Card joker(CardId id) {
        return new Card(id, null, null, true);
    }

    public CardId id() {
        return id;
    }

    public Rank rank() {
        return rank;
    }

    public Suit suit() {
        return suit;
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

    public Card select() {
        return selectable ? this : null;
    }

    public CardSnapshot toSnapshot() {
        return new CardSnapshot(
                id.value(),
                rank == null ? null : rank.name(),
                suit == null ? null : suit.name(),
                joker
        );
    }

    public static Card fromSnapshot(CardSnapshot snapshot) {
        CardId id = new CardId(snapshot.id());

        if (snapshot.joker()) {
            return Card.joker(id);
        }

        return Card.standard(
                id,
                Rank.valueOf(snapshot.rank()),
                Suit.valueOf(snapshot.suit())
        );
    }

    @Override
    public String toString() {
        if (joker) {
            return "Joker #" + id.value();
        }

        return rank + " of " + suit + " #" + id.value();
    }
}
