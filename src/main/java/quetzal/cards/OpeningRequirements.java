package quetzal.cards;

import java.util.List;

/**
 * Compatibility facade for opening requirements.
 *
 * New code should prefer OpeningRequirement.forRound(...). This class remains
 * because earlier project code and tests may still reference OpeningRequirements.
 */
public final class OpeningRequirements {

    private OpeningRequirements() {
    }

    public static List<OpeningRequirement> all() {
        return OpeningRequirement.all();
    }

    public static OpeningRequirement forRound(int roundNumber) {
        return OpeningRequirement.forRound(roundNumber);
    }
}
