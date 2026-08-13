package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/**
 * Dives underwater right after attacking, becoming untargetable until the
 * start of its owner's next turn, when it resurfaces. Starts surfaced
 * (targetable) - a Diver that hasn't attacked yet offers no protection.
 * <p>
 * This is a cycle, not a one-time thing: attack -&gt; submerge -&gt; resurface at
 * next turn start -&gt; attack again -&gt; submerge again, and so on.
 */
public class DiverSigil implements Sigil {

    private boolean submerged;

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getSource() != owner) {
            return;
        }
        if (event.getType() == GameEventType.MOVE) {
            submerged = true;
        } else if (event.getType() == GameEventType.TURN_START) {
            submerged = false;
        }
    }

    @Override
    public boolean preventsTargeting(Card owner) {
        return submerged;
    }

    @Override
    public String getName() {
        return "Diver";
    }
}
