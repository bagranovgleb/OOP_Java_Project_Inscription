package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.pelt.PeltKind;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;

import java.util.Arrays;
import java.util.List;
import com.inscription.util.GameRandom;
import java.util.Random;

/**
 * The Prospector: pick one of three boulders to strike. Since nothing
 * distinguishes the boulders beforehand, the choice is really just flavor -
 * whichever is struck rolls the same two-way outcome: a 1-in-3 (33%) chance
 * of a Golden Sheep Pelt (the SHEEP tier - spendable at the Trader like any
 * other pelt), otherwise a random Insect-tribe creature card.
 */
public class ProspectorEvent implements NodeContent {

    private final Random random;

    public ProspectorEvent() {
        this(GameRandom.create());
    }

    /** Public so tests (including Main.java's own scenarios) can inject a seeded Random for deterministic outcomes. */
    public ProspectorEvent(Random random) {
        this.random = random;
    }

    @Override
    public String describe() {
        return "The Prospector: pick one of three boulders to strike";
    }

    @Override
    public boolean resolve(RunContext context) {
        context.getUI().askChoice("Choose a boulder for the Prospector to strike:",
            List.of("Boulder A", "Boulder B", "Boulder C")); // the choice itself doesn't change the odds - nothing distinguishes them

        Player player = context.getPlayer();
        if (random.nextInt(3) == 0) {
            player.gainPelt(PeltKind.SHEEP);
            context.getUI().show("A Golden Sheep Pelt gleams inside the rock! Golden Sheep Pelts: "
                + player.getPeltCount(PeltKind.SHEEP));
        } else {
            Card reward = randomInsectReward();
            context.getUI().show("An insect was fossilized inside! You found: " + reward);
            player.addToAnimalDeck(reward);
        }
        return true;
    }

    private Card randomInsectReward() {
        List<CardType> insectPool = Arrays.stream(CardType.values())
            .filter(t -> t.create().hasSigil("Insect"))
            .toList();
        return insectPool.get(random.nextInt(insectPool.size())).create();
    }
}
