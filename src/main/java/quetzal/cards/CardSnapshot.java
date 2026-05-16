package quetzal.cards;

public record CardSnapshot(
        int id,
        String rank,
        String suit,
        boolean joker
) {
    public CardSnapshot {
        if (id <= 0) {
            throw new IllegalArgumentException("Card snapshot id must be positive.");
        }

        if (joker && (rank != null || suit != null)) {
            throw new IllegalArgumentException("A joker snapshot cannot have rank or suit.");
        }

        if (!joker && (rank == null || suit == null)) {
            throw new IllegalArgumentException("A standard card snapshot must have rank and suit.");
        }
    }
}
