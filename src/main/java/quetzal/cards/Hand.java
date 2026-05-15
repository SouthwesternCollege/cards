package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hand extends CardCollection {

    private double currentX = 100;  // Starting X position for played cards (initially set to a margin)
    private double currentY = 100;  // Starting Y position for played cards (initially set to a margin)
    private final int DEFAULT_CARD_SPACING = 80;
    private final int COLLAPSE_SPACING = 30;  // Horizontal spacing between cards
    private final int VERTICAL_SPACING = 400;  // Vertical spacing between rows of cards
    private static final double CARD_WIDTH = 142;
    private static final double CARD_TOP_PADDING = 60;
    private static final double SELECTED_CARD_Y_OFFSET = -50;

    private Deck deck;
    private List<Card> selectedCards = new ArrayList<>();  // Keep track of selected cards
    private final Map<CardId, Entity> cardEntities = new HashMap<>();
    private Rectangle2D handArea;
    private int handY;
    private int handX;
    private int cardSpacing;

    public Hand(Rectangle2D handArea, Deck deck) {
        super(new ArrayList<>());
        this.handArea = handArea;
        this.handX = (int) handArea.getMinX();
        this.handY = (int) handArea.getMinY();
        this.cardSpacing = DEFAULT_CARD_SPACING;
        this.deck = deck;
    }

    public void populateHand(int size) {
        // Populate the hand with 'size' number of cards
        for (int i = 0; i < size; i++) {
            Card card = deck.drawCard();

            Entity cardEntity = FXGL.spawn("Card", new SpawnData(handArea.getMinX(), handArea.getMinY() + CARD_TOP_PADDING)
                    .put("card", card)
                    .put("z-index", i)
                    .put("hand", this));

            registerCardEntity(card, cardEntity);
            getCards().add(card);  // Add the card to the hand's list
        }

        organizeCardEntities();
    }
    @Override
    public void addCard(Card card) {
        getCards().add(card);
    }

    @Override
    public Card removeCard(Card card) {
        getCards().remove(card);
        return card;
    }

    public void playSelectedCards() {
        if (selectedCards.isEmpty()) {
            return; // No cards selected
        }

        sortCardsByPosition(selectedCards);

        double screenWidth = FXGL.getAppWidth();

        Duration expansionDuration = Duration.seconds(0.5);
        Duration collapseDuration = Duration.seconds(0.5);

        for (int i = 0; i < selectedCards.size(); i++) {
            Card card = selectedCards.get(i);
            Entity cardEntity = getEntityFor(card);

            // Expand to the "played" area with a slight size increase
            FXGL.animationBuilder()
                    .duration(expansionDuration)
                    .interpolator(Interpolators.SMOOTH.EASE_OUT())
                    .translate(cardEntity)
                    .to(new Point2D(cardEntity.getX(), handY - 200))  // Move to the played area
                    .buildAndPlay();

            // Check if the next card would go beyond the screen width
            if (currentX + COLLAPSE_SPACING > screenWidth) {
                // Wrap to the next line
                currentX = 30;  // Reset X to the left margin
                currentY += VERTICAL_SPACING;  // Move to the next row
            }

            // Collapse to the currentX and currentY position with 10-pixel visibility spacing
            Point2D collapseTarget = new Point2D(currentX, currentY);

            FXGL.animationBuilder()
                    .delay(expansionDuration)  // Delay until after the expansion
                    .duration(collapseDuration)
                    .interpolator(Interpolators.SMOOTH.EASE_IN())
                    .translate(cardEntity)
                    .to(collapseTarget)  // Collapse to the calculated position
                    .buildAndPlay();

            // Disable selection for this card after it is played
            card.setSelectable(false);

            // Move the currentX position for the next card
            currentX += COLLAPSE_SPACING;

            // After the animations, remove the card from the hand
            removeCard(card);
        }

        currentX += 120;

        // Clear the selected cards list
        selectedCards.clear();

        // Reorganize the remaining cards in the hand
        if (!getCards().isEmpty()) {
            organizeCardEntities();
        }
    }

    public List<Card> getSelectedCards() {
        return selectedCards;
    }

    public boolean addSelected(Card card) {
        if (isSelected(card)) {
            return false;
        }

        if (selectedCards.size() < 5) {  // Limit to 5 selected cards
            selectedCards.add(card);
            return true;
        }

        FXGL.getNotificationService().pushNotification("You can only select up to 5 cards!");
        return false;
    }

    // Remove a card from the selected list

    public boolean removeSelected(Card card) {
        // Add additional logic here if needed
        return selectedCards.remove(card);
    }

    public boolean isSelected(Card card) {
        return selectedCards.contains(card);
    }

    public int size() {
        return getCards().size();
    }

    public int getHandY() {
        return handY;
    }

    public int getHandX() {
        return handX;
    }

    public int getCardSpacing() {
        return cardSpacing;
    }

    public Card getCard(int index) {
        return getCards().get(index);
    }

    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();
        for(Card card: getCards()) {
            entities.add(getEntityFor(card));
        }
        return entities;
    }

    public void organizeCardEntities() {
        updateCardSpacing();

        List<Card> cards = getCards();

        for (int i = 0; i < cards.size(); i++) {
            Entity cardEntity = getEntityFor(cards.get(i));
            Point2D targetPosition = getCardVisualPosition(i);

            cardEntity.setZIndex(i);

            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.2))
                    .translate(cardEntity)
                    .to(targetPosition)
                    .buildAndPlay();
        }
    }

    public void organizeCardEntitiesExcept(Card excludedCard) {
        updateCardSpacing();

        List<Card> cards = getCards();

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            if (card == excludedCard) {
                getEntityFor(card).setZIndex(100);
                continue;
            }

            Entity cardEntity = getEntityFor(card);
            Point2D targetPosition = getCardVisualPosition(i);

            cardEntity.setZIndex(i);

            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.12))
                    .translate(cardEntity)
                    .to(targetPosition)
                    .buildAndPlay();
        }
    }

    private void updateCardSpacing() {
        int cardCount = getCards().size();

        if (cardCount <= 1) {
            cardSpacing = DEFAULT_CARD_SPACING;
            return;
        }

        double availableWidth = handArea.getWidth() - CARD_WIDTH;
        double idealSpacing = availableWidth / (cardCount - 1);

        cardSpacing = (int) Math.min(DEFAULT_CARD_SPACING, idealSpacing);
    }

    public Point2D getCardPosition(int index) {
        updateCardSpacing();

        int cardCount = getCards().size();

        if (cardCount == 0) {
            return new Point2D(handArea.getMinX(), handArea.getMinY() + CARD_TOP_PADDING);
        }

        double totalHandWidth = CARD_WIDTH + cardSpacing * (cardCount - 1);
        double startX = handArea.getMinX() + (handArea.getWidth() - totalHandWidth) / 2;
        double y = handArea.getMinY() + CARD_TOP_PADDING;

        return new Point2D(startX + index * cardSpacing, y);
    }

    public Point2D getCardVisualPosition(int index) {
        Point2D position = getCardPosition(index);
        Card card = getCards().get(index);

        if (isSelected(card)) {
            return position.add(0, SELECTED_CARD_Y_OFFSET);
        }

        return position;
    }

    public void sortByRank() {
        getCards().sort(
                Comparator.comparingInt((Card card) -> card.isJoker() ? Integer.MAX_VALUE : card.rank().pokerValue())
                        .thenComparing(card -> card.isJoker() ? null : card.suit(), Comparator.nullsLast(Comparator.naturalOrder()))
        );

        organizeCardEntities();
    }

    public void sortBySuit() {
        getCards().sort(
                Comparator.comparing((Card card) -> card.isJoker() ? null : card.suit(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(card -> card.isJoker() ? Integer.MAX_VALUE : card.rank().pokerValue())
        );

        organizeCardEntities();
    }

    public void sortCardsByPosition(List<Card> cards) {
        cards.sort(Comparator.comparingDouble(card -> getEntityFor(card).getX()));
    }


    public void registerCardEntity(Card card, Entity entity) {
        cardEntities.put(card.id(), entity);
    }

    public Entity getEntityFor(Card card) {
        Entity entity = cardEntities.get(card.id());

        if (entity == null) {
            throw new IllegalStateException("No entity registered for card: " + card);
        }

        return entity;
    }

}
