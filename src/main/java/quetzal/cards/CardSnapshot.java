package quetzal.cards;

public record CardSnapshot(
        int id,
        String rank,
        String suit,
        boolean joker
) {
}
