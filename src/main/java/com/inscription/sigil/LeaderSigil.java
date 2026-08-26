package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * Creatures adjacent to this card (same side, one lane over) gain 1 power -
 * not this card itself. A passive, position-based marker: checked by name in
 * GameEngine.computeEffectiveAttack rather than reacting to an event, since
 * the effect depends on current board position, not something that happened.
 */
public class LeaderSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine.
    }

    @Override
    public String getName() {
        return "Leader";
    }
}
