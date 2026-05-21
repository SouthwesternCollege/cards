package quetzal.cards;

public record MeldId(int value) {

    public MeldId {
        if (value <= 0) {
            throw new IllegalArgumentException("Meld id must be positive.");
        }
    }
}
