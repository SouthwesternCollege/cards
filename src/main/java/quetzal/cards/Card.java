package quetzal.cards;

import com.almasb.fxgl.entity.Entity;

public class Card {

    public static Suit[] suits = {Suit.HEART, Suit.CLUB, Suit.DIAMOND, Suit.SPADE};
    private int cardIndex;
    private Entity entity;
    private boolean selectable = true;

    public Card(int cardIndex) {
        this.cardIndex =cardIndex;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    // Make sure that only selectable cards can be added to the selected list
    public Card select() {
        if (selectable) {
            return this;
            // Add to selectedCards in Hand
        }
        return null;
    }

    // Getter and setter for rank
    public int getCardIndex() {
        return cardIndex;
    }

    public void setCardIndex(int cardIndex) {
        this.cardIndex = cardIndex;
    }


    // Optional: Manage associated Entity
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public int rank() {
        return cardIndex % 13;
    }
}
