package com.inscription.item;

/**
 * Pliers: place a weight on the scales, dealing 1 point of damage to the
 * opponent. Real Inscryption pairs this with a "dagger puzzle" that later
 * replaces it with the Special Dagger and unlocks Act II - that whole
 * meta-progression system isn't modeled here (this project has no acts or
 * puzzle unlocks), so Pliers is simply a straightforward, repeatable
 * 1-damage item.
 */
public class PliersItem implements Item {

    @Override
    public String getName() {
        return "Pliers";
    }

    @Override
    public String getDescription() {
        return "Deal 1 damage to the opponent.";
    }

    @Override
    public boolean use(ItemContext context) {
        context.getEngine().getHealthScale().damageOpponent(1);
        context.getUI().show("A weight settles on the scales - 1 damage dealt.");
        return true;
    }
}
