package com.inscription.item;

import com.inscription.model.Card;
import com.inscription.sigil.TemporaryAirborneSigil;

/**
 * Harpie's Birdleg Fan: every creature currently on the user's board
 * attacks as though Airborne for the rest of this turn - cards placed
 * after using the item are unaffected, since only the board's occupants
 * at the moment of use are touched.
 */
public class HarpiesBirdlegFanItem implements Item {

    @Override
    public String getName() {
        return "Harpie's Birdleg Fan";
    }

    @Override
    public String getDescription() {
        return "Your creatures attack as though Airborne this turn - cards placed after using this are unaffected.";
    }

    @Override
    public boolean use(ItemContext context) {
        var slots = context.getBoard().getPlayerSlots();
        int affected = 0;
        for (var slot : slots) {
            Card occupant = slot.getOccupant();
            if (occupant != null) {
                occupant.addSigil(new TemporaryAirborneSigil());
                affected++;
            }
        }
        if (affected == 0) {
            context.getUI().show("No creatures currently on your board to affect - the item is not used.");
            return false;
        }
        context.getUI().show(affected + " creature(s) will attack as though Airborne this turn.");
        return true;
    }
}
