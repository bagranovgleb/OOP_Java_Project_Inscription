package com.inscryptor.modifier;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;

/**
 * A totem-style modifier: buffs every card on the player's board that carries
 * a specific sigil (e.g. "Squirrel"). This is exactly why tribe sigils
 * exist - the modifier doesn't know or care that a Squirrel is tagged as
 * such via a TribeSigil rather than being a dedicated Java class,
 * it just asks each card "do you have this sigil" and buffs it if so.
 */
public class SigilTotemModifier implements ChallengeModifier {

    private final String targetSigilName;
    private final int attackBonus;
    private final int healthBonus;

    public SigilTotemModifier(String targetSigilName, int attackBonus, int healthBonus) {
        this.targetSigilName = targetSigilName;
        this.attackBonus = attackBonus;
        this.healthBonus = healthBonus;
    }

    @Override
    public void apply(GameContext context) {
        for (var slot : context.getBoard().getPlayerSlots()) {
            Card occupant = slot.getOccupant();
            if (occupant != null && occupant.hasSigil(targetSigilName)) {
                if (attackBonus > 0) {
                    occupant.buffAttack(attackBonus);
                }
                if (healthBonus > 0) {
                    occupant.heal(healthBonus);
                }
            }
        }
    }

    @Override
    public boolean isCompatibleWith(ChallengeModifier other) {
        return true;
    }

    @Override
    public String getName() {
        return "Totem: " + targetSigilName;
    }
}
