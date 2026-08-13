package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

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
