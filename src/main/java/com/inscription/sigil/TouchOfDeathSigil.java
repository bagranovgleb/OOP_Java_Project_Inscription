package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/**
 * This card instantly kills any card it damages, regardless of its own
 * attack stat. Normal attack damage is already applied by the time sigils
 * run (see GameEngine.resolveAttack), so this just finishes the job by
 * zeroing whatever health the target has left.
 */
public class TouchOfDeathSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() == GameEventType.ATTACK && event.getSource() == owner) {
            Card target = event.getTarget();
            if (target != null && target.isAlive()) {
                target.takeDamage(target.getHealth());
            }
        }
    }

    @Override
    public String getName() {
        return "Touch of Death";
    }
}
