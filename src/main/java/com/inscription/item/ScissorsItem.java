package com.inscription.item;

import com.inscription.model.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Scissors: cut up (destroy) one of the opponent's board cards. Only cards
 * currently in play can be cut - nothing in hand is a valid target, and if
 * the opponent's board is entirely empty, the item can't be used.
 */
public class ScissorsItem implements Item {

    @Override
    public String getName() {
        return "Scissors";
    }

    @Override
    public String getDescription() {
        return "Destroy one of the opponent's cards currently on the board.";
    }

    @Override
    public boolean use(ItemContext context) {
        var slots = context.getBoard().getOpponentSlots();
        List<Integer> occupiedLanes = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].getOccupant() != null) {
                occupiedLanes.add(i);
            }
        }
        if (occupiedLanes.isEmpty()) {
            context.getUI().show("No cards on the opponent's side of the board to cut - the item is not used.");
            return false;
        }

        List<String> options = occupiedLanes.stream()
            .map(lane -> "Lane " + lane + ": " + context.getBoard().peekCard(false, lane))
            .toList();
        int choice = context.getUI().askChoice("Choose an opposing card to cut:", options);
        int lane = occupiedLanes.get(choice);
        Card destroyed = context.getBoard().peekCard(false, lane);
        context.getBoard().removeCard(false, lane);
        context.getUI().show(destroyed.getName() + " was destroyed.");
        return true;
    }
}
