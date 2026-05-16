package quetzal.cards;

public enum Rank {
    ACE(1, 20),
    TWO(2, 5),
    THREE(3, 5),
    FOUR(4, 5),
    FIVE(5, 5),
    SIX(6, 5),
    SEVEN(7, 5),
    EIGHT(8, 10),
    NINE(9, 10),
    TEN(10, 10),
    JACK(11, 10),
    QUEEN(12, 10),
    KING(13, 10);

    private final int sequenceValue;
    private final int scoreValue;

    Rank(int sequenceValue, int scoreValue) {
        this.sequenceValue = sequenceValue;
        this.scoreValue = scoreValue;
    }

    public int sequenceValue() {
        return sequenceValue;
    }

    public int scoreValue() {
        return scoreValue;
    }
}
