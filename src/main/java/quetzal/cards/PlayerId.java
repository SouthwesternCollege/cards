package quetzal.cards;

public record PlayerId(int value) {

    public PlayerId {
        if (value <= 0) {
            throw new IllegalArgumentException("Player id must be positive.");
        }
    }
}
