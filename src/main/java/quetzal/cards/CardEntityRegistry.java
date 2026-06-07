package quetzal.cards;

import com.almasb.fxgl.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Presentation-layer registry that maps domain cards to FXGL entities.
 *
 * Keeping this map outside the Card model preserves the core rule that domain
 * cards do not know about FXGL.
 */
public final class CardEntityRegistry {

    private final Map<CardId, Entity> entitiesByCardId = new HashMap<>();

    public void register(Card card, Entity entity) {
        entitiesByCardId.put(card.id(), entity);
    }

    public Entity get(Card card) {
        Entity entity = entitiesByCardId.get(card.id());

        if (entity == null) {
            throw new IllegalStateException("No entity registered for card: " + card);
        }

        return entity;
    }

    public void remove(Card card) {
        entitiesByCardId.remove(card.id());
    }

    public void clear() {
        entitiesByCardId.clear();
    }

    public List<Entity> getAllFor(List<Card> cards) {
        List<Entity> entities = new ArrayList<>();

        for (Card card : cards) {
            entities.add(get(card));
        }

        return entities;
    }
}
