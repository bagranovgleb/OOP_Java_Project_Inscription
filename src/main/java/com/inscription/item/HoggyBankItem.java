package com.inscription.item;

/**
 * Hoggy Bank: immediately gain 4 bones. Normally obtained when one of the
 * player's creatures is destroyed at a Campfire - see CampfireEvent, which
 * grants one directly when that happens, rather than through the usual
 * item pools.
 */
public class HoggyBankItem implements Item {

    @Override
    public String getName() {
        return "Hoggy Bank";
    }

    @Override
    public String getDescription() {
        return "Immediately gain 4 bones.";
    }

    @Override
    public boolean use(ItemContext context) {
        context.getPlayer().gainBones(4);
        context.getUI().show("Cracked open for 4 bones.");
        return true;
    }
}
