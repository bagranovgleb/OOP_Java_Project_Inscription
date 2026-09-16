package com.inscription.item;

import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.sigil.ConjuredMarker;

/** Squirrel in a Bottle: creates a Squirrel (0 power, 1 health) in the user's hand. */
public class SquirrelInABottleItem implements Item {

    @Override
    public String getName() {
        return "Squirrel in a Bottle";
    }

    @Override
    public String getDescription() {
        return "Creates a Squirrel in your hand.";
    }

    @Override
    public boolean use(ItemContext context) {
        Card squirrel = CardType.SQUIRREL.create();
        squirrel.addSigil(new ConjuredMarker()); // conjured, not earned - shouldn't return to the deck after the fight
        context.getPlayer().addToHand(squirrel);
        context.getUI().show("A Squirrel scurries into your hand.");
        return true;
    }
}
