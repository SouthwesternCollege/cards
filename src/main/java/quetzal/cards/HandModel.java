package quetzal.cards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pure hand/domain state.
 *
 * This class intentionally knows nothing about FXGL entities, animation, layout,
 * textures, or mouse input. It answers questions such as:
 *
 * - Which cards are in the hand?
 * - Which cards are selected?
 * - Which cards are selectable?
 * - What is the player-controlled ordering of the hand?
 */
public class HandModel extends CardCollection {

    private final List<Card> selectedCards = new ArrayList<>();
    private final Set<CardId> unselectableCards = new HashSet<>();

    public HandModel() {
        super(new ArrayList<>());
    }

    @Override
    public void addCard(Card card) {
        getCards().add(card);
    }

    @Override
    public Card removeCard(Card card) {
        getCards().remove(card);
        selectedCards.remove(card);
        unselectableCards.remove(card.id());
        return card;
    }

    public Card drawFrom(Deck deck) {
        Card card = deck.drawCard();
        addCard(card);
        return card;
    }

    public List<Card> getSelectedCards() {
        return selectedCards;
    }

    public List<Card> selectedCardsSnapshot() {
        return new ArrayList<>(selectedCards);
    }

    public boolean addSelected(Card card) {
        if (isSelected(card) || !isSelectable(card)) {
            return false;
        }

        selectedCards.add(card);
        return true;
    }

    public boolean removeSelected(Card card) {
        return selectedCards.remove(card);
    }

    public void clearSelected() {
        selectedCards.clear();
    }

    public boolean isSelected(Card card) {
        return selectedCards.contains(card);
    }

    public boolean isSelectable(Card card) {
        return !unselectableCards.contains(card.id());
    }

    public void setSelectable(Card card, boolean selectable) {
        if (selectable) {
            unselectableCards.remove(card.id());
        } else {
            unselectableCards.add(card.id());
            selectedCards.remove(card);
        }
    }

    public int size() {
        return getCards().size();
    }

    public Card getCard(int index) {
        return getCards().get(index);
    }

    public void clear() {
        getCards().clear();
        selectedCards.clear();
        unselectableCards.clear();
    }

    public void replaceCardsPreservingSelection(List<Card> orderedCards) {
        getCards().clear();
        getCards().addAll(orderedCards);

        selectedCards.removeIf(card -> !getCards().contains(card));
        unselectableCards.removeIf(cardId -> getCards().stream().noneMatch(card -> card.id().equals(cardId)));
    }

    public void sortByRank() {
        getCards().sort(
                Comparator.comparingInt((Card card) -> card.isJoker() ? Integer.MAX_VALUE : card.rank().sequenceValue())
                        .thenComparing(card -> card.isJoker() ? null : card.suit(), Comparator.nullsLast(Comparator.naturalOrder()))
        );
    }

    public void sortBySuit() {
        getCards().sort(
                Comparator.comparing((Card card) -> card.isJoker() ? null : card.suit(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(card -> card.isJoker() ? Integer.MAX_VALUE : card.rank().sequenceValue())
        );
    }
}
