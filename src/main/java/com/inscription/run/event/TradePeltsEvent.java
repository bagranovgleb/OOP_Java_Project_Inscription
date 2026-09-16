package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.pelt.PeltKind;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;
import com.inscription.sigil.RandomizableSigils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.inscription.util.GameRandom;
import java.util.Random;

/**
 * Trade Pelts: spend pelts to buy cards from the Trader's market. Works
 * through pelt kinds automatically in increasing order - Rabbit, then
 * Wolf, then Sheep, then their Fused counterparts - letting you spend as
 * many pelts of one kind as you hold before moving to the next, rather
 * than limiting you to a single purchase per visit. A "Leave" option is
 * always available if you want to stop early.
 * <p>
 * Each kind's market is generated once and shrinks as you buy from it
 * (bought cards are removed, not re-rolled), so you're working through one
 * real, fixed list per kind rather than a fresh random draw every
 * purchase. The market shown depends on the kind: Rabbit and Wolf offer up
 * to 8 eligible cards, Sheep up to 4 (matching that kind's cost filter -
 * see PeltKind). A fused kind's market shows fused (double-stat, via
 * Card.stitch on two copies of the same catalog entry) versions of the
 * same eligible cards instead of plain ones - "same rules for market" as
 * its base kind, just fused cards.
 * <p>
 * The Wolf market specifically always includes one constructed card:
 * Rabbit-tier cost, with a random bonus sigil - a judgment call for how
 * many of the 8 slots this affects, since the reference sheet didn't
 * specify a count.
 * <p>
 * FINAL, CONFIRMED RULE STATEMENT (do not reintroduce the earlier, reverted
 * "free pelt purchase" behavior here - that belongs to BuyPeltsEvent, the
 * Trapper, which already grants one free Rabbit Pelt unconditionally on
 * every visit and needs no changes):
 * <ul>
 *   <li>Trapper (BuyPeltsEvent): gives 1 free Rabbit Pelt, always. No
 *   other special behavior. Already correct as-is.</li>
 *   <li>Trader (this class): gives 5 golden teeth if the player has no
 *   pelts of any kind to exchange - nothing else special.</li>
 * </ul>
 */
public class TradePeltsEvent implements NodeContent {

    private static final int NO_PELTS_TEETH_BONUS = 5;

    private final Random random;

    public TradePeltsEvent() {
        this(GameRandom.create());
    }

    /** Public so tests can inject a seeded Random for deterministic outcomes. */
    public TradePeltsEvent(Random random) {
        this.random = random;
    }

    @Override
    public String describe() {
        return "Trade Pelts: spend your pelts for cards from the Trader's market";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();
        boolean hasAnyPelts = Arrays.stream(PeltKind.values()).anyMatch(k -> player.getPeltCount(k) > 0);
        if (!hasAnyPelts) {
            context.getUI().show("You have no pelts to trade - the Trader tosses you "
                + NO_PELTS_TEETH_BONUS + " golden teeth instead.");
            player.gainTeeth(NO_PELTS_TEETH_BONUS);
            return true;
        }

        for (PeltKind kind : PeltKind.values()) {
            if (player.getPeltCount(kind) == 0) {
                continue;
            }

            List<Card> market = generateMarket(kind);
            if (market.isEmpty()) {
                context.getUI().show("No eligible cards for the " + kind.getDisplayName() + " market - skipping.");
                continue;
            }

            while (player.getPeltCount(kind) > 0 && !market.isEmpty()) {
                List<String> optionTexts = new ArrayList<>(market.stream().map(Card::toString).toList());
                optionTexts.add("Leave");
                int choice = context.getUI().askChoice(
                    "You hold " + player.getPeltCount(kind) + " " + kind.getDisplayName() + "(s). The Trader's market:",
                    optionTexts);
                if (choice == market.size()) {
                    return true;
                }
                player.spendPelt(kind);
                Card bought = market.remove(choice);
                player.addToAnimalDeck(bought);
                context.getUI().show("Bought: " + bought);
            }
        }
        context.getUI().show("The Trader has nothing left you can afford.");
        return true;
    }

    private List<Card> generateMarket(PeltKind kind) {
        List<CardType> eligible = new ArrayList<>(Arrays.stream(CardType.values())
            .filter(t -> t != CardType.DAM)
            .filter(t -> kind.matchesCostFilter(t.create()))
            .toList());
        Collections.shuffle(eligible, random);

        List<Card> market = new ArrayList<>();
        int size = Math.min(kind.getMarketSize(), eligible.size());
        for (int i = 0; i < size; i++) {
            CardType type = eligible.get(i);
            market.add(kind.isFused() ? Card.stitch(type.create(), type.create()) : type.create());
        }

        // Wolf-tier special: one slot is always a constructed cheap (Rabbit-tier cost) card with a random bonus sigil.
        if (kind == PeltKind.WOLF) {
            Card special = randomCheapCardWithSigil();
            if (market.size() >= kind.getMarketSize() && !market.isEmpty()) {
                market.set(market.size() - 1, special);
            } else {
                market.add(special);
            }
        }

        return market;
    }

    private Card randomCheapCardWithSigil() {
        List<CardType> rabbitTierPool = Arrays.stream(CardType.values())
            .filter(t -> t != CardType.DAM)
            .filter(t -> PeltKind.RABBIT.matchesCostFilter(t.create()))
            .toList();
        CardType base = rabbitTierPool.get(random.nextInt(rabbitTierPool.size()));
        Card card = base.create();
        card.addSigil(RandomizableSigils.randomExcluding(random, card));
        return card;
    }
}
