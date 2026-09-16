package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * Base class for sigils that are pure markers - they don't react to any
 * game event themselves. Instead, GameEngine (or another sigil's board
 * scan) checks for their presence by name at the exact moment it matters
 * (e.g. "does the attacker have Airborne?", "does this dying card have
 * Bone King?"). Extending this instead of implementing Sigil directly
 * means each of these classes only has to supply its own getName() - the
 * empty apply() that every one of them needs is written once, here.
 */
public abstract class PassiveMarkerSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name elsewhere in the engine, not reactive.
    }
}
