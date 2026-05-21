package quetzal.cards;

import java.util.ArrayList;
import java.util.List;

public final class Meld {

    private final MeldId id;
    private final PlayerId createdBy;
    private final List<Card> cards;

    public Meld(MeldId id, PlayerId createdBy, List<Card> cards) {
        if (id == null) {
            throw new IllegalArgumentException("Meld id cannot be null.");
        }

        if (createdBy == null) {
            throw new IllegalArgumentException("Meld creator cannot be null.");
        }

        if (cards == null) {
            throw new IllegalArgumentException("Meld cards cannot be null.");
        }

        if (cards.size() < 3) {
            throw new IllegalArgumentException("A meld must contain at least three cards.");
        }

        if (cards.stream().anyMatch(card -> card == null)) {
            throw new IllegalArgumentException("A meld cannot contain null cards.");
        }

        this.id = id;
        this.createdBy = createdBy;
        this.cards = new ArrayList<>(cards);
    }

    public MeldId id() {
        return id;
    }

    public PlayerId createdBy() {
        return createdBy;
    }

    public List<Card> cards() {
        return List.copyOf(cards);
    }

    public int size() {
        return cards.size();
    }

    public boolean contains(CardId cardId) {
        return cards.stream().anyMatch(card -> card.id().equals(cardId));
    }

    public void addCards(List<Card> cardsToAdd, MeldPlacement placement) {
        if (cardsToAdd == null || cardsToAdd.isEmpty()) {
            throw new IllegalArgumentException("Cards to add cannot be null or empty.");
        }

        if (cardsToAdd.stream().anyMatch(card -> card == null)) {
            throw new IllegalArgumentException("Cards to add cannot contain null cards.");
        }

        if (placement == null) {
            throw new IllegalArgumentException("Meld placement cannot be null.");
        }

        switch (placement) {
            case BEGINNING -> cards.addAll(0, cardsToAdd);
            case END, ANYWHERE -> cards.addAll(cardsToAdd);
        }
    }

    public Card replaceCard(CardId cardToReplaceId, Card replacementCard) {
        if (cardToReplaceId == null) {
            throw new IllegalArgumentException("Card to replace id cannot be null.");
        }

        if (replacementCard == null) {
            throw new IllegalArgumentException("Replacement card cannot be null.");
        }

        for (int i = 0; i < cards.size(); i++) {
            Card currentCard = cards.get(i);

            if (currentCard.id().equals(cardToReplaceId)) {
                cards.set(i, replacementCard);
                return currentCard;
            }
        }

        throw new IllegalArgumentException("Card is not in meld: " + cardToReplaceId);
    }
}
