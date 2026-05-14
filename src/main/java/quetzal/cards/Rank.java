package quetzal.cards;

public enum Rank {
    ACE("Ace", "A", 14, 0),
    TWO("Two", "2", 2, 1),
    THREE("Three", "3", 3, 2),
    FOUR("Four", "4", 4, 3),
    FIVE("Five", "5", 5, 4),
    SIX("Six", "6", 6, 5),
    SEVEN("Seven", "7", 7, 6),
    EIGHT("Eight", "8", 8, 7),
    NINE("Nine", "9", 9, 8),
    TEN("Ten", "10", 10, 9),
    JACK("Jack", "J", 11, 10),
    QUEEN("Queen", "Q", 12, 11),
    KING("King", "K", 13, 12);

    private final String displayName;
    private final String symbol;
    private final int pokerValue;
    private final int spriteColumn;

    Rank(String displayName, String symbol, int pokerValue, int spriteColumn) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.pokerValue = pokerValue;
        this.spriteColumn = spriteColumn;
    }

    public String displayName() {
        return displayName;
    }

    public String symbol() {
        return symbol;
    }

    public int pokerValue() {
        return pokerValue;
    }

    public int spriteColumn() {
        return spriteColumn;
    }

    public static Rank fromSpriteColumn(int spriteColumn) {
        for (Rank rank : values()) {
            if (rank.spriteColumn == spriteColumn) {
                return rank;
            }
        }

        throw new IllegalArgumentException("Invalid rank sprite column: " + spriteColumn);
    }
}
