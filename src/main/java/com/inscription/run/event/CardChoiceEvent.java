package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.ResourceType;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * "Choose between three cards," in the three flavors from the reference
 * sheet: PLAIN (see all three before picking), TRIBE (pick a tribe blind,
 * get a random card from it), and COST (pick a cost tier blind, get a
 * random card at that cost). Whatever's won goes straight into the animal
 * deck, ready to be drawn in a future battle - not into hand, since a card
 * reward should be found in a future battle, not available immediately.
 */
public class CardChoiceEvent implements NodeContent {

    public enum Mode { PLAIN, TRIBE, COST }

    private static final List<String> KNOWN_TRIBES = List.of("Avian", "Canine", "Hooved", "Insect", "Reptile");

    private final Mode mode;
    private final Random random = new Random();

    public CardChoiceEvent(Mode mode) {
        this.mode = mode;
    }

    @Override
    public String describe() {
        return switch (mode) {
            case PLAIN -> "Card Choice: pick one of three cards (you can see all three first)";
            case TRIBE -> "Card Choice (tribe): pick a tribe blind, get a random card from it";
            case COST -> "Card Choice (cost): pick a cost tier blind, get a random card at that cost";
        };
    }

    @Override
    public boolean resolve(RunContext context) {
        switch (mode) {
            case PLAIN -> resolvePlain(context);
            case TRIBE -> resolveTribe(context);
            case COST -> resolveCost(context);
        }
        return true;
    }

    private void resolvePlain(RunContext context) {
        List<CardType> options = pickDistinctCardTypes(3);
        List<String> optionTexts = options.stream().map(t -> t.create().toString()).toList();
        int choice = context.getUI().askChoice("Choose a card to add to your deck:", optionTexts);
        grantCard(context, options.get(choice).create());
    }

    private void resolveTribe(RunContext context) {
        List<String> tribes = pickDistinctTribes(3);
        int choice = context.getUI().askChoice("Choose a tribe (the specific card is a surprise until after you pick):", tribes);
        String chosenTribe = tribes.get(choice);
        CardType reward = randomCardTypeMatching(t -> t.create().hasSigil(chosenTribe));
        Card card = reward.create();
        context.getUI().show("Revealed: " + card);
        grantCard(context, card);
    }

    private void resolveCost(RunContext context) {
        List<CostCategory> categories = pickDistinctCostCategories(3);
        List<String> categoryLabels = categories.stream().map(CostCategory::getLabel).toList();
        int choice = context.getUI().askChoice("Choose a cost (the specific card is a surprise until after you pick):", categoryLabels);
        CostCategory category = categories.get(choice);
        CardType reward = randomCardTypeMatching(t -> categorize(t.create()) == category);
        Card card = reward.create();
        context.getUI().show("Revealed: " + card);
        grantCard(context, card);
    }

    private void grantCard(RunContext context, Card card) {
        context.getPlayer().addToAnimalDeck(card);
        context.getUI().show(card.getName() + " has been shuffled into your deck.");
    }

    private List<CardType> pickDistinctCardTypes(int count) {
        List<CardType> pool = new ArrayList<>(rewardPool());
        Collections.shuffle(pool, random);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    private List<String> pickDistinctTribes(int count) {
        List<String> tribes = new ArrayList<>(KNOWN_TRIBES);
        Collections.shuffle(tribes, random);
        return tribes.subList(0, Math.min(count, tribes.size()));
    }

    private List<CostCategory> pickDistinctCostCategories(int count) {
        Set<CostCategory> present = new LinkedHashSet<>();
        for (CardType t : rewardPool()) {
            present.add(categorize(t.create()));
        }
        List<CostCategory> categories = new ArrayList<>(present);
        Collections.shuffle(categories, random);
        return categories.subList(0, Math.min(count, categories.size()));
    }

    private CardType randomCardTypeMatching(Predicate<CardType> predicate) {
        List<CardType> matches = rewardPool().stream().filter(predicate).toList();
        return matches.get(random.nextInt(matches.size()));
    }

    /** Every ordinary card except Dam, which is a sigil-spawned obstacle, not a real reward. */
    private List<CardType> rewardPool() {
        return Arrays.stream(CardType.values())
            .filter(t -> t != CardType.DAM)
            .toList();
    }

    /**
     * Groups a card's exact cost into one of 4 named tiers, rather than
     * offering the exact number - the player picks a named category blind,
     * not a specific amount. Bones costs are never subdivided further:
     * every Bones-cost card, regardless of exact amount, falls into one
     * single "bones" category.
     */
    private CostCategory categorize(Card card) {
        if (card.getCostType() == ResourceType.BONES) {
            return CostCategory.BONES;
        }
        int amount = card.getCost();
        if (amount <= 1) {
            return CostCategory.LITTLE_BLOOD;
        }
        if (amount == 2) {
            return CostCategory.AVERAGE_BLOOD;
        }
        return CostCategory.A_LOT_OF_BLOOD;
    }

    private enum CostCategory {
        LITTLE_BLOOD("little blood"),
        AVERAGE_BLOOD("average blood"),
        A_LOT_OF_BLOOD("a lot of blood"),
        BONES("bones");

        private final String label;

        CostCategory(String label) {
            this.label = label;
        }

        String getLabel() {
            return label;
        }
    }
}
