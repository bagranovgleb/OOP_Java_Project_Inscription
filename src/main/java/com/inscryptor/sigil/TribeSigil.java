package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;

/**
 * Marks a card as belonging to a tribe (e.g. "Wolf", "Squirrel", "Avian").
 * Does nothing on its own - no on-attack, on-death, or targeting effect - it
 * exists purely so totem-style modifiers can find "every card of tribe X"
 * via {@code card.hasSigil(name)}. Hidden from the normal sigil UI since it
 * has no visible effect.
 * <p>
 * One reusable class instead of a dedicated WolfSigil/SquirrelSigil/etc. per
 * tribe - the tribe name is data, not behavior, so it doesn't need its own
 * class.
 */
public class TribeSigil implements Sigil {

    private final String tribeName;

    public TribeSigil(String tribeName) {
        this.tribeName = tribeName;
    }

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // No active effect - identity only.
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public String getName() {
        return tribeName;
    }
}
