package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * This card counts as 3 blood rather than 1 when sacrificed. A passive
 * marker: Player.sacrificeCard checks for it by name rather than this class
 * doing anything reactively.
 */
public class WorthySacrificeSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name at the point of sacrifice.
    }

    @Override
    public String getName() {
        return "Worthy Sacrifice";
    }
}
