package com.inscription.item;

import com.inscription.model.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Fish Hook: steal one of the opponent's board cards onto an empty space on
 * your own side.
 * <p>
 * Deliberately NOT added to GainConsumablesEvent's pool or
 * TrinketBearerSigil's pool - the reference sheet explicitly excludes it
 * from both, since in the real game it only becomes available after
 * defeating a specific boss ("The Angler") once. This project has no boss
 * encounters or run-count tracking, so that unlock condition isn't
 * modeled; the mechanic itself is built and correct, but currently has no
 * normal in-game path to actually obtain one (it can still be granted
 * directly, e.g. via player.addItem(new FishHookItem()), for testing or
 * once a boss-unlock system exists).
 */
public class FishHookItem implements Item {

    @Override
    public String getName() {
        return "Fish Hook";
    }

    @Override
    public String getDescription() {
        return "Steal one of the opponent's board cards onto an empty space on your own side.";
    }

    @Override
    public boolean use(ItemContext context) {
        var opponentSlots = context.getBoard().getOpponentSlots();
        List<Integer> opponentOccupiedLanes = new ArrayList<>();
        for (int i = 0; i < opponentSlots.length; i++) {
            if (opponentSlots[i].getOccupant() != null) {
                opponentOccupiedLanes.add(i);
            }
        }
        if (opponentOccupiedLanes.isEmpty()) {
            context.getUI().show("The opponent has no cards to hook - the item is not used.");
            return false;
        }

        var playerSlots = context.getBoard().getPlayerSlots();
        List<Integer> emptyOwnLanes = new ArrayList<>();
        for (int i = 0; i < playerSlots.length; i++) {
            if (playerSlots[i].getOccupant() == null) {
                emptyOwnLanes.add(i);
            }
        }
        if (emptyOwnLanes.isEmpty()) {
            context.getUI().show("You have no empty space to receive a hooked card - the item is not used.");
            return false;
        }

        List<String> sourceOptions = opponentOccupiedLanes.stream()
            .map(lane -> "Lane " + lane + ": " + context.getBoard().peekCard(false, lane))
            .toList();
        int sourceChoice = context.getUI().askChoice("Choose an opposing card to hook:", sourceOptions);
        int sourceLane = opponentOccupiedLanes.get(sourceChoice);

        List<String> targetOptions = emptyOwnLanes.stream().map(lane -> "Lane " + lane).toList();
        int targetChoice = context.getUI().askChoice("Choose an empty space on your side to receive it:", targetOptions);
        int targetLane = emptyOwnLanes.get(targetChoice);

        Card hooked = context.getBoard().removeCard(false, sourceLane);
        context.getBoard().placeCard(true, targetLane, hooked);
        context.getUI().show(hooked.getName() + " is now yours.");
        return true;
    }
}
