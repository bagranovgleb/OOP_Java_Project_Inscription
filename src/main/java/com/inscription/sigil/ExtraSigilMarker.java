package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * A hidden marker with no active effect - added to a card once it's
 * received a sigil transfer from the Mysterious Stones run event, so that
 * event can refuse to offer it as a target a second time ("you may not
 * choose any cards that already have extra sigils"). Same pattern as
 * TribeSigil: existence is the whole point, not behavior.
 */
public class ExtraSigilMarker implements Sigil {

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
        return "Extra Sigil";
    }
}
