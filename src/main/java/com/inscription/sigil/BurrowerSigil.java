package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * This card will move to any empty space that's attacked by an enemy, to
 * block it. A passive marker: GameEngine.resolveLaneAttack checks for it by
 * name and does the actual relocation, since a sigil can't move its own
 * card between board slots on its own.
 */
public class BurrowerSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine.
    }

    @Override
    public String getName() {
        return "Burrower";
    }
}
