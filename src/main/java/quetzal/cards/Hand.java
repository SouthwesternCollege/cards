package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import javafx.animation.PauseTransition;
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
    private final MeldLayout meldLayout;
    private final VisualMeldStore visualMeldStore;
    private final CardEntityRegistry entityRegistry;
    private final Rectangle2D playerPlayedArea;
    private final Deck deck;
    private final MeldValidator meldValidator = new LaKikaMeldValidator();
    private final SelectionFeedback selectionFeedback;
    private final HandChangeListener handChangeListener;
    private final PlayerId localPlayerId = new PlayerId(1);
    private int nextDebugCardId = 10_000;

    public Hand(Rectangle2D handArea, Rectangle2D playerPlayedArea, Deck deck) {
        this(handArea, playerPlayedArea, deck, new NoOpSelectionFeedback(), new NoOpHandChangeListener());
    }

    public Hand(Rectangle2D handArea, Rectangle2D playerPlayedArea, Deck deck, SelectionFeedback selectionFeedback) {
        this(handArea, playerPlayedArea, deck, selectionFeedback, new NoOpHandChangeListener());
    }

    public Hand(
            Rectangle2D handArea,
            Rectangle2D playerPlayedArea,
            Deck deck,
            SelectionFeedback selectionFeedback,
            HandChangeListener handChangeListener
    ) {
        this.model = new HandModel();
        this.layout = new HandLayout(handArea);
        this.meldLayout = new MeldLayout();
        this.visualMeldStore = new VisualMeldStore();
        this.entityRegistry = new CardEntityRegistry();
        this.playerPlayedArea = playerPlayedArea;
        this.deck = deck;
        this.selectionFeedback = selectionFeedback == null ? new NoOpSelectionFeedback() : selectionFeedback;
        this.handChangeListener = handChangeListener == null ? new NoOpHandChangeListener() : handChangeListener;
    }

    /**
     * Compatibility constructor for older call sites.
     * Prefer Hand(Rectangle2D handArea, Rectangle2D playerPlayedArea, Deck deck).
     */
    public Hand(Rectangle2D handArea, Deck deck) {
        this(handArea, new Rectangle2D(handArea.getMinX(), handArea.getMinY() - 260, handArea.getWidth(), 220), deck);
    }


    public void populateHand(List<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("Cards cannot be null.");
        }

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            model.addCard(card);
            Point2D spawnPosition = layout.basePosition(i, cards.size());

            Entity cardEntity = FXGL.spawn("Card", new SpawnData(spawnPosition.getX(), spawnPosition.getY())
                    .put("card", card)
                    .put("z-index", i)
                    .put("hand", this));

            registerCardEntity(card, cardEntity);
        }

        organizeCardEntities();
        notifyHandSizeChanged();
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
        notifyHandSizeChanged();
    }



    public void addCardFromSource(Card card, Point2D sourcePosition) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null.");
        }

        if (sourcePosition == null) {
            throw new IllegalArgumentException("Source position cannot be null.");
        }

        model.addCard(card);
        List<Card> cardsAfterAdd = model.getCards();
        int newCardIndex = cardsAfterAdd.size() - 1;
        Point2D target = layout.visualPosition(newCardIndex, cardsAfterAdd, selectedCardIds());

        Entity cardEntity = FXGL.spawn("Card", new SpawnData(sourcePosition.getX(), sourcePosition.getY())
                .put("card", card)
                .put("z-index", newCardIndex)
                .put("hand", this));

        cardEntity.setOpacity(0.0);
        registerCardEntity(card, cardEntity);

        Entity backEntity = FXGL.entityBuilder(new SpawnData(sourcePosition.getX(), sourcePosition.getY()))
                .view(new CardBackViewFactory().createCardBackView())
                .zIndex(500)
                .buildAndAttach();

        FXGL.animationBuilder()
                .duration(Duration.seconds(AnimationSettings.PLAYED_CARD_MOVE_SECONDS))
                .interpolator(Interpolators.SMOOTH.EASE_OUT())
                .translate(backEntity)
                .to(target)
                .buildAndPlay();

        FXGL.runOnce(() -> {
            backEntity.removeFromWorld();
            cardEntity.setPosition(target);
            cardEntity.setOpacity(1.0);
            organizeCardEntities();
        }, Duration.seconds(AnimationSettings.PLAYED_CARD_MOVE_SECONDS));

        notifyHandSizeChanged();
    }

    /**
     * Prototype draw behavior for Milestone 4D.
     *
     * This intentionally ignores full turn legality. Later this should delegate
     * to GameState / TurnController.
     */
    public void drawOneCardFromDeck(Point2D sourcePosition) {
        if (deck.getCards().isEmpty()) {
            deck.addShuffledStandardDeckWithJokers();
        }

        addCardFromSource(deck.drawCard(), sourcePosition);
    }

    public void addCard(Card card) {
        model.addCard(card);
        notifyHandSizeChanged();
    }

    public Card removeCard(Card card) {
        Card removed = model.removeCard(card);
        notifyHandSizeChanged();
        return removed;
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

        visualMeldStore.add(new VisualMeld(localPlayerId, cardsToPlay));

        for (Card card : cardsToPlay) {
            Entity cardEntity = getEntityFor(card);
            model.setSelectable(card, false);
            model.removeCard(card);
            disableHandInteraction(cardEntity);
        }

        reflowPlayedMelds();

        model.clearSelected();
        selectionFeedback.selectionChanged(model.selectedCardsSnapshot());

        notifyHandSizeChanged();

        if (!model.getCards().isEmpty()) {
            organizeCardEntities();
        }
    }


    /**
     * Development-only helper for stress-testing the played-meld layout without
     * needing full draw/discard/turn systems. Do not use this as game logic.
     */
    public void debugAddKindMeld() {
        int sequence = visualMeldStore.all().size();
        Rank rank = Rank.values()[sequence % Rank.values().length];
        Suit firstSuit = Suit.values()[sequence % Suit.values().length];
        Suit secondSuit = Suit.values()[(sequence + 1) % Suit.values().length];
        Suit thirdSuit = Suit.values()[(sequence + 2) % Suit.values().length];

        addDebugVisualMeld(List.of(
                Card.standard(nextDebugCardId(), rank, firstSuit),
                Card.standard(nextDebugCardId(), rank, secondSuit),
                Card.standard(nextDebugCardId(), rank, thirdSuit)
        ));
    }

    /**
     * Development-only helper for adding a valid straight flush. The cards are
     * already in sequence order so layout behavior can be inspected directly.
     */
    public void debugAddStraightFlushMeld() {
        int sequence = visualMeldStore.all().size();
        Suit suit = Suit.values()[sequence % Suit.values().length];
        int start = 1 + (sequence % 8);

        addDebugVisualMeld(List.of(
                Card.standard(nextDebugCardId(), rankWithSequenceValue(start), suit),
                Card.standard(nextDebugCardId(), rankWithSequenceValue(start + 1), suit),
                Card.standard(nextDebugCardId(), rankWithSequenceValue(start + 2), suit),
                Card.standard(nextDebugCardId(), rankWithSequenceValue(start + 3), suit)
        ));
    }

    /**
     * Development-only stress tool for quickly testing wrapping, centering,
     * overlap, and staggered animation.
     */
    public void debugAddManyMelds() {
        for (int i = 0; i < 8; i++) {
            int sequence = visualMeldStore.all().size();

            if (i % 2 == 0) {
                Rank rank = Rank.values()[sequence % Rank.values().length];
                addDebugVisualMeld(List.of(
                        Card.standard(nextDebugCardId(), rank, Suit.CLUB),
                        Card.standard(nextDebugCardId(), rank, Suit.DIAMOND),
                        Card.standard(nextDebugCardId(), rank, Suit.HEART)
                ), false);
            } else {
                Suit suit = Suit.values()[sequence % Suit.values().length];
                int start = 1 + (sequence % 8);
                addDebugVisualMeld(List.of(
                        Card.standard(nextDebugCardId(), rankWithSequenceValue(start), suit),
                        Card.standard(nextDebugCardId(), rankWithSequenceValue(start + 1), suit),
                        Card.standard(nextDebugCardId(), rankWithSequenceValue(start + 2), suit),
                        Card.standard(nextDebugCardId(), rankWithSequenceValue(start + 3), suit),
                        Card.standard(nextDebugCardId(), rankWithSequenceValue(start + 4), suit)
                ), false);
            }
        }

        reflowPlayedMelds();
    }

    /**
     * Development-only cleanup tool. This removes all visual meld cards currently
     * in the played area.
     */
    public void debugClearVisualMelds() {
        for (VisualMeld meld : visualMeldStore.all()) {
            for (Card card : meld.cards()) {
                Entity entity = getEntityFor(card);
                if (entity != null) {
                    entity.removeFromWorld();
                }
            }
        }

        visualMeldStore.clear();
    }

    private void addDebugVisualMeld(List<Card> cards) {
        addDebugVisualMeld(cards, true);
    }

    private void addDebugVisualMeld(List<Card> cards, boolean reflowAfterAdd) {
        Point2D spawnPosition = new Point2D(
                playerPlayedArea.getMinX() + playerPlayedArea.getWidth() / 2.0 - CardViewMetrics.renderedWidth() / 2.0,
                playerPlayedArea.getMinY() + playerPlayedArea.getHeight() / 2.0 - CardViewMetrics.renderedHeight() / 2.0
        );

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            Entity cardEntity = FXGL.spawn("Card", new SpawnData(spawnPosition.getX(), spawnPosition.getY())
                    .put("card", card)
                    .put("z-index", 300 + i)
                    .put("hand", this));

            registerCardEntity(card, cardEntity);
            disableHandInteraction(cardEntity);
        }

        visualMeldStore.add(new VisualMeld(localPlayerId, cards));

        if (reflowAfterAdd) {
            reflowPlayedMelds();
        }
    }

    private void reflowPlayedMelds() {
        List<MeldLayoutSlot> playedSlots = meldLayout.slots(visualMeldStore.meldsFor(localPlayerId), playerPlayedArea);

        for (MeldLayoutSlot slot : playedSlots) {
            Entity cardEntity = getEntityFor(slot.card());

            if (cardEntity == null) {
                continue;
            }

            animatePlayedCard(cardEntity, slot);
        }
    }

    private CardId nextDebugCardId() {
        return new CardId(nextDebugCardId++);
    }

    private Rank rankWithSequenceValue(int sequenceValue) {
        for (Rank rank : Rank.values()) {
            if (rank.sequenceValue() == sequenceValue) {
                return rank;
            }
        }

        throw new IllegalArgumentException("Unsupported rank sequence value: " + sequenceValue);
    }


    private void animatePlayedCard(Entity cardEntity, MeldLayoutSlot slot) {
        double delaySeconds = slot.zIndex() * AnimationSettings.PLAYED_CARD_STAGGER_SECONDS;
        Point2D target = slot.position();

        PauseTransition delay = new PauseTransition(Duration.seconds(delaySeconds));
        delay.setOnFinished(event -> {
            FXGL.animationBuilder()
                    .duration(Duration.seconds(AnimationSettings.PLAYED_CARD_MOVE_SECONDS))
                    .interpolator(Interpolators.SMOOTH.EASE_OUT())
                    .translate(cardEntity)
                    .to(target)
                    .buildAndPlay();

            cardEntity.setZIndex(100 + slot.zIndex());
        });
        delay.play();
    }

    private void notifyHandSizeChanged() {
        handChangeListener.handSizeChanged(model.size());
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
