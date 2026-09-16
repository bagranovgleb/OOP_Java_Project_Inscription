package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * A hidden marker with no active effect - added to a card the instant one
 * of the "in a bottle" items (Squirrel, Boulder, Black Goat, Frozen
 * Opossum) conjures it into hand. Battle's own end-of-fight reshuffle
 * (Player.reshuffleHandIntoDecks()) checks for this marker and skips
 * returning that specific card to either deck - it just disappears, since
 * it was only ever meant to exist for this one fight, not become a real,
 * permanent addition to the player's collection.
 * <p>
 * Marking the individual Card instance this way - rather than, say,
 * checking the card's name or type - is what lets a card conjured by an
 * item and the same card type earned legitimately elsewhere (a Black Goat
 * from a Bone Altar boon, a reward from Card Choice) coexist correctly:
 * only the specific instance that actually came out of a bottle carries
 * this marker, so only that one vanishes. Same pattern as TribeSigil and
 * ExtraSigilMarker: existence is the whole point, not behavior.
 */
public class ConjuredMarker implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // No active effect - marker only.
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public String getName() {
        return "Conjured";
    }
}
