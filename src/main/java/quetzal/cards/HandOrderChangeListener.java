package quetzal.cards;

import java.util.List;

/**
 * Presentation-to-domain callback used while Hand remains transitional.
 */
public interface HandOrderChangeListener {

    void handOrderChanged(List<Card> orderedCards);
}
