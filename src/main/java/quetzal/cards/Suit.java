package quetzal.cards;

public enum Suit {
    HEART(0), CLUB(1), DIAMOND(2), SPADE(3);

    private int suit;

    Suit(int suit){
        this.suit = suit;
    }
    public int getSuit() {
        return suit;
    }
}
