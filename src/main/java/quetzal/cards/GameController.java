package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlled mutation boundary for GameState.
 *
 * Milestone 5A keeps this intentionally small. Later, UI code should request
 * actions through this controller instead of directly mutating state.
 */
public final class GameController {

    private static final int DEFAULT_PLAYER_COUNT = 4;
    private static final int DEFAULT_HAND_SIZE = 13;
    private static final int DEFAULT_CASTIGOS_PER_GAME = 10;

    private final GameState gameState;

    private GameController(GameState gameState) {
        this.gameState = gameState;
    }

    public static GameController newPrototypeGame() {
        Deck deck = Deck.laKikaPrototypeDeck();
        deck.shuffle();

        List<PlayerState> players = new ArrayList<>();

        for (int i = 1; i <= DEFAULT_PLAYER_COUNT; i++) {
            PlayerId playerId = new PlayerId(i);
            players.add(new PlayerState(
                    playerId,
                    "Player " + i,
                    0,
                    DEFAULT_CASTIGOS_PER_GAME,
                    false
            ));
        }

        PlayerId dealerId = new PlayerId(1);
        PlayerId activePlayerId = new PlayerId(2);

        RoundState roundState = new RoundState(
                1,
                dealerId,
                activePlayerId,
                TurnPhase.DRAW_OR_CASTIGO
        );

        GameState state = new GameState(players, deck, roundState);
        GameController controller = new GameController(state);
        controller.dealInitialHands(DEFAULT_HAND_SIZE);

        return controller;
    }

    public GameState state() {
        return gameState;
    }

    public List<PlayerHudState> hudStates() {
        return gameState.toHudStates();
    }

    public List<Card> handFor(PlayerId playerId) {
        return gameState.player(playerId).hand();
    }

    public Deck deck() {
        return gameState.deck();
    }

    private void dealInitialHands(int handSize) {
        for (int cardIndex = 0; cardIndex < handSize; cardIndex++) {
            for (PlayerState player : gameState.players()) {
                if (gameState.deck().getCards().isEmpty()) {
                    gameState.deck().addShuffledStandardDeckWithJokers();
                }

                player.addCard(gameState.deck().drawCard());
            }
        }
    }
}
