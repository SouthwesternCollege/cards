package quetzal.cards;

public final class NoOpHandChangeListener implements HandChangeListener {
    @Override
    public void handSizeChanged(int cardsRemaining) {
        // Intentionally empty.
    }
}
