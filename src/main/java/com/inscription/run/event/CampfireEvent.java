package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;

import java.util.List;
import java.util.Random;

/**
 * Campfire: rest a card from hand to permanently buff it - +1 attack for a
 * DAMAGE fire, +2 health for a HEALTH fire.
 * <p>
 * The first rest is a guaranteed improvement - since it can never go wrong,
 * there's no option to leave before taking it; that choice would be
 * meaningless. Once you've rested a card, you may leave with what you have,
 * or rest that SAME card a second time for a 50/50 - another improvement,
 * or the card is destroyed outright. You cannot switch to a different card
 * partway through: whichever card you pick for the first rest is the only
 * one you can risk again at this fire.
 */
public class CampfireEvent implements NodeContent {

    public enum Type { DAMAGE, HEALTH }

    private final Type type;
    private final Random random;

    public CampfireEvent(Type type) {
        this(type, new Random());
    }

    /** Public so tests (including Main.java's own scenarios) can inject a seeded Random for deterministic outcomes. */
    public CampfireEvent(Type type, Random random) {
        this.type = type;
        this.random = random;
    }

    @Override
    public String describe() {
        return "Campfire (" + type.name().toLowerCase()
            + "): rest a card for a guaranteed improvement, with the option to risk it once more for a 50/50";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();
        List<Card> drawnForThisEvent = HandUtil.drawEntireDeckIntoHand(player);
        List<Card> hand = player.getHand();
        if (hand.isEmpty()) {
            context.getUI().show("Your hand is empty, and your deck has nothing left to draw either - nothing to rest at the fire.");
            return true;
        }

        List<String> handTexts = hand.stream().map(Card::toString).toList();
        int choice = context.getUI().askChoice("Resting a card here guarantees it an improvement. Choose one:", handTexts);
        Card chosenCard = hand.get(choice);
        restCard(chosenCard, player, true, context); // first rest: guaranteed

        if (!player.getHand().contains(chosenCard)) {
            HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
            return true; // defensive - a guaranteed rest should never destroy the card
        }

        int again = context.getUI().askChoice(
            "Rest " + chosenCard.getName() + " again? This is a 50/50: another improvement, "
                + "or the card is destroyed. You can only risk this same card, not switch to a different one.",
            List.of("Rest again (take the risk)", "Leave with what you have"));
        if (again == 0) {
            restCard(chosenCard, player, false, context); // second rest: 50/50
        }
        HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
        return true;
    }

    private void restCard(Card card, Player player, boolean guaranteed, RunContext context) {
        if (!guaranteed && random.nextDouble() < 0.5) {
            context.getUI().show(card.getName() + " was destroyed by the fire!");
            player.removeFromHand(card);
            if (player.addItem(new com.inscription.item.HoggyBankItem())) {
                context.getUI().show("A Hoggy Bank was left behind in the ashes.");
            }
            return;
        }
        if (type == Type.DAMAGE) {
            card.buffAttack(1);
        } else {
            card.buffMaxHealth(2);
        }
        context.getUI().show(card.getName() + " rested safely: " + card);
    }
}
