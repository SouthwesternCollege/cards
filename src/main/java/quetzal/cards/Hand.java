package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Transitional presentation facade for the player's visible hand.
 *
 * Milestone 4A intentionally keeps this class name so the rest of the prototype
 * still works, but it now delegates domain state to HandModel and layout math to
 * HandLayout. The remaining FXGL spawning/animation code will later move into a
 * dedicated HandView/HandController.
 */
public class Hand {

    private final HandModel model;
    private final HandLayout layout;
    private final PlayedMeldLayout playedMeldLayout;
    private final CardEntityRegistry entityRegistry;
    private final Rectangle2D playerPlayedArea;
    private final Deck deck;
    private final MeldValidator meldValidator = new LaKikaMeldValidator();
    private final SelectionFeedback selectionFeedback;
    private final List<List<Card>> playedMelds = new ArrayList<>();

    public Hand(Rectangle2D handArea, Rectangle2D playerPlayedArea, Deck deck) {
        this(handArea, playerPlayedArea, deck, new HudMeldSelectionFeedback(new LaKikaMeldValidator()));
    }

    public Hand(Rectangle2D handArea, Rectangle2D playerPlayedArea, Deck deck, SelectionFeedback selectionFeedback) {
        this.model = new HandModel();
        this.layout = new HandLayout(handArea);
        this.playedMeldLayout = new PlayedMeldLayout();
        this.entityRegistry = new CardEntityRegistry();
        this.playerPlayedArea = playerPlayedArea;
        this.deck = deck;
        this.selectionFeedback = selectionFeedback == null ? new NoOpSelectionFeedback() : selectionFeedback;
    }

    /**
     * Compatibility constructor for older call sites.
     * Prefer Hand(Rectangle2D handArea, Rectangle2D playerPlayedArea, Deck deck).
     */
    public Hand(Rectangle2D handArea, Deck deck) {
        this(handArea, new Rectangle2D(handArea.getMinX(), handArea.getMinY() - 260, handArea.getWidth(), 220), deck);
    }

    public void populateHand(int size) {
        for (int i = 0; i < size; i++) {
            Card card = model.drawFrom(deck);
            Point2D spawnPosition = layout.basePosition(i, size);

            Entity cardEntity = FXGL.spawn("Card", new SpawnData(spawnPosition.getX(), spawnPosition.getY())
                    .put("card", card)
                    .put("z-index", i)
                    .put("hand", this));

            registerCardEntity(card, cardEntity);
        }

        organizeCardEntities();
    }

    public void addCard(Card card) {
        model.addCard(card);
    }

    public Card removeCard(Card card) {
        return model.removeCard(card);
    }

    public void playSelectedCards() {
        if (model.getSelectedCards().isEmpty()) {
            return;
        }

        List<Card> selectedSnapshot = model.selectedCardsSnapshot();
        MeldValidationResult validationResult = meldValidator.validate(selectedSnapshot);

        if (!validationResult.valid()) {
            selectionFeedback.invalidPlayAttempt(validationResult);
            return;
        }

        List<Card> cardsToPlay = orderedCardsForPlayedMeld(selectedSnapshot, validationResult);
        Set<CardId> newlyPlayedCardIds = cardIds(cardsToPlay);

        playedMelds.add(List.copyOf(cardsToPlay));
        List<CardLayoutSlot> playedSlots = playedMeldLayout.centeredSlotsForMelds(playedMelds, playerPlayedArea);

        Duration playDuration = Duration.seconds(0.45);

        for (CardLayoutSlot slot : playedSlots) {
            Card card = slot.card();
            Entity cardEntity = getEntityFor(card);
            Point2D target = slot.visualPosition();

            if (newlyPlayedCardIds.contains(card.id())) {
                model.setSelectable(card, false);
                model.removeCard(card);
                disableHandInteraction(cardEntity);
            }

            FXGL.animationBuilder()
                    .duration(playDuration)
                    .interpolator(Interpolators.SMOOTH.EASE_OUT())
                    .translate(cardEntity)
                    .to(target)
                    .buildAndPlay();

            cardEntity.setZIndex(100 + slot.zIndex());
        }

        model.clearSelected();
        selectionFeedback.selectionChanged(model.selectedCardsSnapshot());

        if (!model.getCards().isEmpty()) {
            organizeCardEntities();
        }
    }


    private Set<CardId> cardIds(List<Card> cards) {
        Set<CardId> ids = new HashSet<>();

        for (Card card : cards) {
            ids.add(card.id());
        }

        return ids;
    }

    private void disableHandInteraction(Entity cardEntity) {
        cardEntity.getComponent(CardAnimationComponent.class).setInteractionEnabled(false);
    }

    private List<Card> orderedCardsForPlayedMeld(List<Card> selectedSnapshot, MeldValidationResult validationResult) {
        if (validationResult.valid() && validationResult.meldType() == MeldType.STRAIGHT_FLUSH) {
            return validationResult.normalizedCards();
        }

        // Kind melds preserve selection/insertion order.
        return selectedSnapshot;
    }

    public List<Card> getSelectedCards() {
        return model.getSelectedCards();
    }

    public boolean addSelected(Card card) {
        boolean added = model.addSelected(card);

        if (added) {
            selectionFeedback.selectionChanged(model.selectedCardsSnapshot());
        }

        return added;
    }

    public boolean removeSelected(Card card) {
        boolean removed = model.removeSelected(card);

        if (removed) {
            selectionFeedback.selectionChanged(model.selectedCardsSnapshot());
        }

        return removed;
    }

    public SelectionChange toggleSelected(Card card) {
        if (model.isSelected(card)) {
            if (model.removeSelected(card)) {
                selectionFeedback.selectionChanged(model.selectedCardsSnapshot());
                return SelectionChange.DESELECTED;
            }

            return SelectionChange.UNCHANGED;
        }

        if (model.addSelected(card)) {
            selectionFeedback.selectionChanged(model.selectedCardsSnapshot());
            return SelectionChange.SELECTED;
        }

        return SelectionChange.UNCHANGED;
    }

    public boolean isSelected(Card card) {
        return model.isSelected(card);
    }

    public boolean isSelectable(Card card) {
        return model.isSelectable(card);
    }

    public void setSelectable(Card card, boolean selectable) {
        model.setSelectable(card, selectable);
    }

    public int size() {
        return model.size();
    }

    public int getHandY() {
        return (int) layout.handArea().getMinY();
    }

    public int getHandX() {
        return (int) layout.handArea().getMinX();
    }

    public double getCardSpacing() {
        return layout.cardSpacing(model.size());
    }

    public Card getCard(int index) {
        return model.getCard(index);
    }

    public List<Card> getCards() {
        return model.getCards();
    }

    public List<Entity> getEntities() {
        return entityRegistry.getAllFor(model.getCards());
    }

    public void organizeCardEntities() {
        for (CardLayoutSlot slot : currentLayoutSlots()) {
            Entity cardEntity = getEntityFor(slot.card());
            cardEntity.setZIndex(slot.zIndex());

            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.2))
                    .translate(cardEntity)
                    .to(slot.visualPosition())
                    .buildAndPlay();
        }
    }

    public void organizeCardEntitiesExcept(Card excludedCard) {
        for (CardLayoutSlot slot : currentLayoutSlots()) {
            Entity cardEntity = getEntityFor(slot.card());

            if (slot.card() == excludedCard) {
                cardEntity.setZIndex(100);
                continue;
            }

            cardEntity.setZIndex(slot.zIndex());

            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.12))
                    .translate(cardEntity)
                    .to(slot.visualPosition())
                    .buildAndPlay();
        }
    }

    private List<CardLayoutSlot> currentLayoutSlots() {
        return layout.slots(model.getCards(), selectedCardIds());
    }

    private Set<CardId> selectedCardIds() {
        Set<CardId> selectedCardIds = new HashSet<>();

        for (Card card : model.getSelectedCards()) {
            selectedCardIds.add(card.id());
        }

        return selectedCardIds;
    }

    public Point2D getCardPosition(int index) {
        return layout.basePosition(index, model.size());
    }

    public Point2D getCardVisualPosition(int index) {
        return layout.visualPosition(index, model.getCards(), selectedCardIds());
    }

    public void sortByRank() {
        model.sortByRank();
        organizeCardEntities();
    }

    public void sortBySuit() {
        model.sortBySuit();
        organizeCardEntities();
    }

    public void sortCardsByPosition(List<Card> cards) {
        cards.sort(Comparator.comparingDouble(card -> getEntityFor(card).getX()));
    }

    public void registerCardEntity(Card card, Entity entity) {
        entityRegistry.register(card, entity);
    }

    public Entity getEntityFor(Card card) {
        return entityRegistry.get(card);
    }
}
