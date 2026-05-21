package quetzal.cards;

public enum MeldValidationError {
    TOO_FEW_CARDS("Select at least three cards."),
    NULL_CARD("A meld cannot contain null cards."),
    TOO_MANY_JOKERS("Jokers cannot be more than half of a meld."),
    ALL_JOKERS("A meld cannot contain only jokers."),
    MIXED_RANKS("A kind meld cannot contain mixed non-joker ranks."),
    MIXED_SUITS("A straight flush cannot contain mixed non-joker suits."),
    DUPLICATE_SEQUENCE_RANK("A straight flush cannot contain duplicate sequence ranks."),
    NO_CONSECUTIVE_SEQUENCE("The cards cannot form a consecutive straight flush."),
    CONSECUTIVE_JOKERS("Jokers cannot be consecutive in a straight flush."),
    NO_VALID_MELD_TYPE("The selected cards do not form a valid La Kika meld.");

    private final String message;

    MeldValidationError(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
