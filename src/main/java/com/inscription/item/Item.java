package com.inscription.item;

/**
 * A "Consumable Item" - held in a player's capped 3-slot inventory, usable
 * during a battle. Same shape as Sigil: the engine (here, Battle) never
 * needs to know which concrete item it's calling, just that it can describe
 * itself and be used.
 */
public interface Item {

    String getName();

    String getDescription();

    /**
     * Uses the item. Returns whether it was actually consumed - almost
     * always true, since these are one-shot consumables, but an
     * implementation can return false if the use failed for some reason
     * (e.g. nothing valid to target), so the player doesn't lose it for
     * nothing.
     */
    boolean use(ItemContext context);
}
