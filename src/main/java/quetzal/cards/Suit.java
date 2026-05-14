package quetzal.cards;

public enum Suit {
    HEART("Heart", "Hearts", 0),
    CLUB("Club", "Clubs", 1),
    DIAMOND("Diamond", "Diamonds", 2),
    SPADE("Spade", "Spades", 3);

    private final String displayName;
    private final String pluralDisplayName;
    private final int spriteRow;

    Suit(String displayName, String pluralDisplayName, int spriteRow) {
        this.displayName = displayName;
        this.pluralDisplayName = pluralDisplayName;
        this.spriteRow = spriteRow;
    }

    public String displayName() {
        return displayName;
    }

    public String pluralDisplayName() {
        return pluralDisplayName;
    }

    public int spriteRow() {
        return spriteRow;
    }

    public static Suit fromSpriteRow(int spriteRow) {
        for (Suit suit : values()) {
            if (suit.spriteRow == spriteRow) {
                return suit;
            }
        }

        throw new IllegalArgumentException("Invalid suit sprite row: " + spriteRow);
    }
}
