package com.inscription.item;

import com.inscription.model.Card;
import com.inscription.model.SpecialCardType;
import com.inscription.sigil.ConjuredMarker;

import java.util.ArrayList;
import java.util.List;

/** Places a Boulder (a 0/5 blocker) onto an empty lane on the player's own board. */
public class BoulderItem implements Item {

    @Override
    public String getName() {
        return "Boulder";
    }

    @Override
    public String getDescription() {
        return "Place a Boulder onto an empty lane on your board.";
    }

    @Override
    public boolean use(ItemContext context) {
        var slots = context.getBoard().getPlayerSlots();
        List<Integer> emptyLanes = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].getOccupant() == null) {
                emptyLanes.add(i);
            }
        }
        if (emptyLanes.isEmpty()) {
            context.getUI().show("No empty lane to place the Boulder in - the item is not used.");
            return false;
        }

        List<String> laneOptions = emptyLanes.stream().map(i -> "Lane " + i).toList();
        int choice = context.getUI().askChoice("Choose an empty lane for the Boulder:", laneOptions);
        int lane = emptyLanes.get(choice);
        Card boulder = SpecialCardType.BOULDER.create();
        boulder.addSigil(new ConjuredMarker()); // conjured, not earned - shouldn't return to the deck after the fight
        context.getEngine().placeCard(true, lane, boulder);
        context.getUI().show("Placed a Boulder in lane " + lane + ".");
        return true;
    }
}
