package com.inscription.item;

import com.inscription.model.Card;

import java.util.NoSuchElementException;

/** Peeks at the top card of the player's animal deck without drawing it. */
public class MagnifyingGlassItem implements Item {

    @Override
    public String getName() {
        return "Magnifying Glass";
    }

    @Override
    public String getDescription() {
        return "Peek at the top card of your animal deck without drawing it.";
    }

    @Override
    public boolean use(ItemContext context) {
        try {
            Card top = context.getPlayer().peekAnimalDeck();
            context.getUI().show("The top of your animal deck is: " + top);
            return true;
        } catch (NoSuchElementException e) {
            context.getUI().show("Your animal deck is empty - nothing to see. The item is not used.");
            return false;
        }
    }
}
