package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/** Damages whoever attacks the card carrying this sigil. */
public class SharpQuillsSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() == GameEventType.ATTACK && event.getTarget() == owner) {
            Card attacker = event.getSource();
            if (attacker != null) {
                attacker.takeDamage(owner.getAttack());
            }
        }
    }

    @Override
    public String getName() {
        return "Sharp Quills";
    }
}
