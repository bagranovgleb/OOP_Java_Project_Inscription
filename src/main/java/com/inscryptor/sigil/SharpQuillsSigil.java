package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;
import com.inscryptor.model.GameEventType;

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
