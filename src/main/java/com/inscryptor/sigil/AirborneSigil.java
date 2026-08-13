package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;

/**
 * This card will ignore opposing cards and strike the defending player
 * directly - unless the defender carries Mighty Leap. A passive marker: no
 * reactive behavior of its own. GameEngine.resolveLaneAttack checks for it
 * by name when resolving an attack.
 */
public class AirborneSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine, not reactive.
    }

    @Override
    public String getName() {
        return "Airborne";
    }
}
