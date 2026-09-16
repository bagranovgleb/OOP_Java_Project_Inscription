package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.DeckType;
import com.inscription.model.ResourceType;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;
import com.inscription.sigil.RandomizableSigils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import com.inscription.util.GameRandom;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Deck Trial: pick a trial from 3 offered at random out of 6 possible,
 * then draw 3 cards from your animal deck to see if they collectively
 * satisfy it. Passing grants a choice of 3 cards, each with one random
 * bonus sigil (Kaycee's Mod's "two sigils" variant isn't modeled - this
 * project has no game-mode variants). The drawn cards stay in hand either
 * way - a real draw happened, win or lose.
 * <p>
 * The 6 trials below (Bones, Blood, Power, Health, Wisdom, Kin) are the
 * real Inscryption conditions, not invented ones. Three notes on how they
 * translate to this project specifically:
 * <ul>
 *   <li>Pelts aren't Cards here - they're a separate resource held directly
 *       on Player, never placed into a deck - so "Pelts contribute 0 Power"
 *       is automatically true: a pelt can never be drawn in the first
 *       place.</li>
 *   <li>An Ant-tribe card's Power normally scales with friendly Ants on the
 *       board, which doesn't exist during an event. Here it's approximated
 *       as the count of Ant-tribe cards among the 3 drawn - the closest
 *       analog available without a battle in progress.</li>
 *   <li>Totem-granted sigils never count toward Trial of Wisdom, matching
 *       the real game's own note - and this falls out naturally from this
 *       project's design anyway, since a totem's sigil is only ever granted
 *       the moment a card is placed on a board, never while sitting in a
 *       deck.</li>
 * </ul>
 * Kaycee's Mod's "2 identical cards count as kin" update to Trial of Kin
 * isn't modeled either, for the same game-mode reason.
 */
public class DeckTrialEvent implements NodeContent {

    private record Condition(String description, Predicate<List<Card>> check) {
    }

    private static final List<String> KNOWN_TRIBES = List.of(
        "Avian", "Canine", "Hooved", "Insect", "Reptile", "Squirrel", "Ant");

    private final List<Condition> conditions = List.of(
        new Condition("Trial of Bones: the 3 drawn cards must cost at least 5 Bones combined",
            cards -> sumCost(cards, ResourceType.BONES) >= 5),
        new Condition("Trial of Blood: the 3 drawn cards must cost at least 4 Blood combined",
            cards -> sumCost(cards, ResourceType.BLOOD) >= 4),
        new Condition("Trial of Power: the 3 drawn cards must have at least 4 Power combined",
            cards -> sumPower(cards) >= 4),
        new Condition("Trial of Health: the 3 drawn cards must have at least 6 Health combined",
            cards -> cards.stream().mapToInt(Card::getHealth).sum() >= 6),
        new Condition("Trial of Wisdom: the 3 drawn cards must have at least 3 Sigils combined",
            cards -> sumSigils(cards) >= 3),
        new Condition("Trial of Kin: at least 2 of the 3 drawn cards must share a tribe",
            DeckTrialEvent::hasKin));

    private final Random random;

    public DeckTrialEvent() {
        this(GameRandom.create());
    }

    /** Public so tests can inject a seeded Random for deterministic outcomes. */
    public DeckTrialEvent(Random random) {
        this.random = random;
    }

    @Override
    public String describe() {
        return "Deck Trial: pick a trial, draw 3 cards, and see if they pass";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();

        List<Condition> offeredConditions = new ArrayList<>(conditions);
        Collections.shuffle(offeredConditions, random);
        offeredConditions = offeredConditions.subList(0, 3);

        List<String> conditionTexts = offeredConditions.stream().map(Condition::description).toList();
        int choice = context.getUI().askChoice("Choose a trial:", conditionTexts);
        Condition chosen = offeredConditions.get(choice);

        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            try {
                drawn.add(player.drawFromDeck(DeckType.ANIMAL));
            } catch (NoSuchElementException e) {
                break;
            }
        }

        context.getUI().show("You drew: " + drawn);
        boolean passed = chosen.check().test(drawn);
        if (!passed) {
            context.getUI().show("The trial failed - the condition wasn't met. You keep the drawn cards.");
            return true;
        }

        context.getUI().show("The trial succeeded! Choose a reward:");
        List<Card> rewards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Card reward = randomCardTypeExcludingDam().create();
            reward.addSigil(RandomizableSigils.randomExcluding(random, reward));
            rewards.add(reward);
        }
        List<String> rewardTexts = rewards.stream().map(Card::toString).toList();
        int rewardChoice = context.getUI().askChoice("Choose a reward:", rewardTexts);
        Card chosenReward = rewards.get(rewardChoice);
        player.addToAnimalDeck(chosenReward);
        context.getUI().show("Added to your deck: " + chosenReward);
        return true;
    }

    /** Cards with no cost, or costing the other resource type, contribute 0. */
    private static int sumCost(List<Card> cards, ResourceType type) {
        return cards.stream()
            .filter(c -> c.getCost() > 0 && c.getCostType() == type)
            .mapToInt(Card::getCost)
            .sum();
    }

    /**
     * Ant-tribe cards contribute Power equal to how many Ant-tribe cards
     * were drawn (the closest analog to the board-based swarm count, since
     * no board exists during an event). Pelts can never appear here at all,
     * since this project never places a pelt into a deck as a real card.
     */
    private static int sumPower(List<Card> cards) {
        long antCount = cards.stream().filter(c -> c.hasSigil("Ant")).count();
        int total = 0;
        for (Card c : cards) {
            total += c.hasSigil("Ant") ? (int) antCount : c.getAttack();
        }
        return total;
    }

    /** Visible sigils only - hidden tribe tags and the internal Extra Sigil marker don't count as real abilities. */
    private static int sumSigils(List<Card> cards) {
        return cards.stream()
            .mapToInt(c -> (int) c.getVisibleSigils().stream()
                .filter(s -> !s.getName().equals("Extra Sigil"))
                .count())
            .sum();
    }

    private static boolean hasKin(List<Card> cards) {
        for (String tribe : KNOWN_TRIBES) {
            long count = cards.stream().filter(c -> c.hasSigil(tribe)).count();
            if (count >= 2) {
                return true;
            }
        }
        return false;
    }

    private CardType randomCardTypeExcludingDam() {
        CardType[] pool = Arrays.stream(CardType.values())
            .filter(t -> t != CardType.DAM)
            .toArray(CardType[]::new);
        return pool[random.nextInt(pool.length)];
    }
}
