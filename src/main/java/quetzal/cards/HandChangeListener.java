package quetzal.cards;

@FunctionalInterface
public interface HandChangeListener {
    void handSizeChanged(int cardsRemaining);
}
