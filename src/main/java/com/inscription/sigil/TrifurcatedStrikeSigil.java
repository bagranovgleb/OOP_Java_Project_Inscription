package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * This card deals damage to the opposing spaces left, right, and directly
 * across from it - three lanes total. A passive marker:
 * GameEngine.resolveLaneAttack checks for it by name to pick target lanes.
 */
public class TrifurcatedStrikeSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine.
    }

    @Override
    public String getName() {
        return "Trifurcated Strike";
    }
}
