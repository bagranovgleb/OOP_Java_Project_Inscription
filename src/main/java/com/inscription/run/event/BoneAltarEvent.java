package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;

import java.util.List;

/**
 * Bone Altar: choose a card to permanently remove, granting a Minor Boon of
 * the Bone Lord - +1 bonus bone at the start of every future battle.
 * Sacrificing specifically a Black Goat grants the full Boon of the Bone
 * Lord instead - +4 bonus bones per battle. Multiple visits stack, each
 * adding its own bonus on top of any earlier ones (see
 * Player.gainBonusStartingBones() and Battle's constructor, which grants
 * the accumulated total at the start of every fight).
 * <p>
 * Operates on hand rather than the sealed deck, like every other event
 * that removes or transforms a specific card (Mycologists, Mysterious
 * Stones, Campfire) - Deck deliberately can't be searched or have an
 * arbitrary card pulled out of it.
 */
public class BoneAltarEvent implements NodeContent {

    private static final int MINOR_BOON_BONES = 1;
    private static final int FULL_BOON_BONES = 4;

    @Override
    public String describe() {
        return "Bone Altar: permanently remove a card for a Boon of the Bone Lord";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();
        List<Card> drawnForThisEvent = HandUtil.drawEntireDeckIntoHand(player);
        List<Card> hand = player.getHand();
        if (hand.isEmpty()) {
            context.getUI().show("Your hand is empty, and your deck has nothing left to draw either - nothing to offer the altar.");
            return true;
        }

        List<String> handTexts = hand.stream().map(Card::toString).toList();
        int choice = context.getUI().askChoice("Choose a card to permanently remove:", handTexts);
        Card removed = hand.get(choice);
        player.removeFromHand(removed);

        if (removed.getName().equals("Black Goat")) {
            player.gainBonusStartingBones(FULL_BOON_BONES);
            context.getUI().show("The Black Goat's sacrifice grants the Boon of the Bone Lord: +"
                + FULL_BOON_BONES + " bones at the start of every future battle.");
        } else {
            player.gainBonusStartingBones(MINOR_BOON_BONES);
            context.getUI().show(removed.getName() + " is gone forever. You receive the Minor Boon of the Bone Lord: +"
                + MINOR_BOON_BONES + " bone at the start of every future battle.");
        }
        HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
        return true;
    }
}
