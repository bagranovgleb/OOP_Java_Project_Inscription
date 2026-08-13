package com.inscription.modifier;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;

/** Toughens every opponent card currently on the board by adding bonus health. */
public class ToughEnemiesModifier implements ChallengeModifier {

    private final int bonusHealth;

    public ToughEnemiesModifier(int bonusHealth) {
        this.bonusHealth = bonusHealth;
    }

    @Override
    public void apply(GameContext context) {
        for (var slot : context.getBoard().getOpponentSlots()) {
            Card occupant = slot.getOccupant();
            if (occupant != null) {
                occupant.heal(bonusHealth);
            }
        }
    }

    @Override
    public boolean isCompatibleWith(ChallengeModifier other) {
        return true;
    }

    @Override
    public String getName() {
        return "Tough Enemies";
    }
}
