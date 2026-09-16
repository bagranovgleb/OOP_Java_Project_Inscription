package com.inscription.item;

import com.inscription.model.Card;
import com.inscription.model.SpecialCardType;
import com.inscription.sigil.ConjuredMarker;

/** Boulder in a Bottle: creates a Boulder (0 power, 5 health) in the user's hand. */
public class BoulderInABottleItem implements Item {

    @Override
    public String getName() {
        return "Boulder in a Bottle";
    }

    @Override
    public String getDescription() {
        return "Creates a Boulder in your hand.";
    }

    @Override
    public boolean use(ItemContext context) {
        Card boulder = SpecialCardType.BOULDER.create();
        boulder.addSigil(new ConjuredMarker()); // conjured, not earned - shouldn't return to the deck after the fight
        context.getPlayer().addToHand(boulder);
        context.getUI().show("A Boulder tumbles into your hand.");
        return true;
    }
}
