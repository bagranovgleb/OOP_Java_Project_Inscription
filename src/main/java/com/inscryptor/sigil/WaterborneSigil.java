package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;

/**
 * On the opponent's turn, creatures attacking this card's space attack
 * directly instead - this card never blocks. Unlike Diver (which cycles
 * between submerged and surfaced), this is permanent: reuses the same
 * preventsTargeting hook, just always on.
 */
public class WaterborneSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // No reactive effect - the pass-through behavior is a passive check
        // (preventsTargeting), not something triggered by an event.
    }

    @Override
    public boolean preventsTargeting(Card owner) {
        return true;
    }

    @Override
    public String getName() {
        return "Waterborne";
    }
}
