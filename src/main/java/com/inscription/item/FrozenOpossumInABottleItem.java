package com.inscription.item;

import com.inscription.model.Card;
import com.inscription.model.SpecialCardType;
import com.inscription.sigil.ConjuredMarker;

/** Frozen Opossum in a Bottle: creates a Frozen Opossum (0 power, 5 health, Frozen Away) in the user's hand. */
public class FrozenOpossumInABottleItem implements Item {

    @Override
    public String getName() {
        return "Frozen Opossum in a Bottle";
    }

    @Override
    public String getDescription() {
        return "Creates a Frozen Opossum in your hand.";
    }

    @Override
    public boolean use(ItemContext context) {
        Card opossum = SpecialCardType.FROZEN_OPOSSUM.create();
        opossum.addSigil(new ConjuredMarker()); // conjured, not earned - shouldn't return to the deck after the fight
        context.getPlayer().addToHand(opossum);
        context.getUI().show("A Frozen Opossum thaws its way into your hand.");
        return true;
    }
}
