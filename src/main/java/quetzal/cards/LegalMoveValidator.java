package quetzal.cards;

public interface LegalMoveValidator {

    MoveResult validate(Move move, RoundState roundState, PlayArea playArea);
}
