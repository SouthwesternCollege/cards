package quetzal.cards;

public record CardId(int value) {

    public CardId {
        if (value <= 0) {
            throw new IllegalArgumentException("Card id must be positive.");
        }
    }
}
