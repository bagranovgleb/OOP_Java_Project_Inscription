package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * When this card is sacrificed, it does not perish - the owner still gains
 * the usual resources, but the card stays alive on the board. A passive
 * marker: Player.sacrificeCard and GameEngine.sacrificeFromBoard check for
 * it by name rather than this class doing anything reactively.
 */
public class ManyLivesSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name at the point of sacrifice.
    }

    @Override
    public String getName() {
        return "Many Lives";
    }
}
