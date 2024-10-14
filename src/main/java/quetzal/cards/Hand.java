package quetzal.cards;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Hand extends CardCollection {

    private double currentX = 100;  // Starting X position for played cards (initially set to a margin)
    private double currentY = 100;  // Starting Y position for played cards (initially set to a margin)
    private final int DEFAULT_CARD_SPACING = 80;
    private final int COLLAPSE_SPACING = 30;  // Horizontal spacing between cards
    private final int VERTICAL_SPACING = 400;  // Vertical spacing between rows of cards

    private Deck deck;
    private List<Card> selectedCards = new ArrayList<>();  // Keep track of selected cards
    private int handY;
    private int handX;
    private int cardSpacing;

    public Hand(int handX, int handY, Deck deck) {
        super(new ArrayList<>());
        this.handX = handX;
        this.handY = handY;
        this.cardSpacing = DEFAULT_CARD_SPACING;
        this.deck = deck;
    }

    public void populateHand(int size) {
        cardSpacing = Math.min(DEFAULT_CARD_SPACING, (FXGL.getAppWidth()/3) / (size - 1));

        // Populate the hand with 'size' number of cards
        for (int i = 0; i < size; i++) {
            Card card = deck.drawCard();

            Entity cardEntity = FXGL.spawn("Card", new SpawnData(handX + i * cardSpacing, handY)
                    .put("card", card)
                    .put("z-index", i)
                    .put("hand", this));


            card.setEntity(cardEntity);
            getCards().add(card);  // Add the card to the hand's list
        }
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
            Entity cardEntity = card.getEntity();

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

//            // Optionally, scale the card slightly larger during the expansion, then back to normal
//            FXGL.animationBuilder()
//                    .duration(expansionDuration)
//                    .interpolator(Interpolators.SMOOTH.EASE_OUT())
//                    .scale(cardEntity)
//                    .from(new Point2D(1, 1))  // Normal size
//                    .to(new Point2D(1.2, 1.2))  // Slightly larger (20% increase)
//                    .buildAndPlay();
//
//            // After the delay, shrink back to normal size during the collapse
//            FXGL.animationBuilder()
//                    .delay(expansionDuration)
//                    .duration(collapseDuration)
//                    .interpolator(Interpolators.SMOOTH.EASE_IN())
//                    .scale(cardEntity)
//                    .to(new Point2D(1, 1))  // Back to normal size
//                    .buildAndPlay();

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
        cardSpacing = (FXGL.getAppWidth() /3  - 72) / (this.size() - 1);
        if (getCards().size() > 0) {
            getCards().get(0)
                    .getEntity()
                    .getComponent(CardAnimationComponent.class)
                    .organizeCards();
        }
    }

    public List<Card> getSelectedCards() {
        return selectedCards;
    }

    public int size() {
        return getCards().size();
    }

    public void addSelected(Card card) {
        if (selectedCards.size() < 5) {  // Limit to 5 selected cards
            selectedCards.add(card);
        } else {
            FXGL.getNotificationService().pushNotification("You can only select up to 5 cards!");
        }
    }

    // Remove a card from the selected list
    public void removeSelected(Card card) {
        selectedCards.remove(card);
        // You can add additional logic here if needed
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
            entities.add(card.getEntity());
        }
        return entities;
    }

    public void sortCardsByPosition(List<Card> cards) {
        cards.sort(Comparator.comparingDouble(card -> card.getEntity().getX()));
    }

}
