package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * This card strikes each opposing space to the left and right of the space
 * directly across from it - not that space itself. A passive marker:
 * GameEngine.resolveLaneAttack checks for it by name to pick target lanes.
 */
public class BifurcatedStrikeSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine.
    }

    @Override
    public String getName() {
        return "Bifurcated Strike";
    }
}
