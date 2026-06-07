package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level rules-level state for the game.
 *
 * GameState is the source of truth for what is true in the game. It should not
 * know about FXGL entities, JavaFX nodes, animation, or screen layout.
 */
public final class GameState {

    private final List<PlayerState> players;
    private final Deck deck;
    private final DiscardPile discardPile;
    private final PlayArea playArea;
    private final RoundState roundState;

    public GameState(List<PlayerState> players, Deck deck, DiscardPile discardPile, PlayArea playArea, RoundState roundState) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players cannot be empty.");
        }

        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null.");
        }

        if (discardPile == null) {
            throw new IllegalArgumentException("Discard pile cannot be null.");
        }

        if (playArea == null) {
            throw new IllegalArgumentException("Play area cannot be null.");
        }

        if (roundState == null) {
            throw new IllegalArgumentException("Round state cannot be null.");
        }

        this.players = new ArrayList<>(players);
        this.deck = deck;
        this.discardPile = discardPile;
        this.playArea = playArea;
        this.roundState = roundState;
    }

    public List<PlayerState> players() {
        return List.copyOf(players);
    }

    public PlayerState player(PlayerId playerId) {
        for (PlayerState player : players) {
            if (player.playerId().equals(playerId)) {
                return player;
            }
        }

        throw new IllegalArgumentException("Unknown player id: " + playerId.value());
    }

    public Deck deck() {
        return deck;
    }

    public DiscardPile discardPile() {
        return discardPile;
    }

    public PlayArea playArea() {
        return playArea;
    }

    public RoundState roundState() {
        return roundState;
    }

    public PlayerId nextPlayerAfter(PlayerId playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).playerId().equals(playerId)) {
                int nextIndex = (i + 1) % players.size();
                return players.get(nextIndex).playerId();
            }
        }

        throw new IllegalArgumentException("Unknown player id: " + playerId.value());
    }

    public List<PlayerHudState> toHudStates() {
        List<PlayerHudState> hudStates = new ArrayList<>();

        for (PlayerState player : players) {
            hudStates.add(player.toHudState(roundState.dealerId(), roundState.activePlayerId()));
        }

        return hudStates;
    }
}
