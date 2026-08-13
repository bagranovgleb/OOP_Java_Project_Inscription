package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;

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
