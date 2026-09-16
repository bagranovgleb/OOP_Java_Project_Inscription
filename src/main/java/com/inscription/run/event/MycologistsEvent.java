package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.pelt.PeltKind;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;

import java.util.ArrayList;
import java.util.List;
import com.inscription.util.GameRandom;
import java.util.Random;

/**
 * The Mycologists: fuse two matching ANIMAL cards in hand into one
 * stronger card (combined attack, combined max health, every sigil from
 * both - Squirrels are excluded, they're not "animals" in the sense this
 * event cares about), OR fuse two pelts of the same base kind into one
 * Fused pelt of that kind (see PeltKind) - a Fused pelt unlocks a market
 * of fused/stitched animal cards at the Trader.
 * <p>
 * If nothing is fusable (no matching card pair, no pelt pair), the player
 * instead receives a duplicate of a random animal card they already have
 * in hand, rather than the visit doing nothing.
 * <p>
 * Operates on hand rather than the sealed deck, since Deck deliberately
 * can't be searched or have an arbitrary card pulled out of it.
 */
public class MycologistsEvent implements NodeContent {

    private static final List<PeltKind> BASE_PELT_KINDS = List.of(PeltKind.RABBIT, PeltKind.WOLF, PeltKind.SHEEP);

    private final Random random;

    public MycologistsEvent() {
        this(GameRandom.create());
    }

    /** Public so tests can inject a seeded Random for deterministic outcomes. */
    public MycologistsEvent(Random random) {
        this.random = random;
    }

    @Override
    public String describe() {
        return "The Mycologists: fuse two matching animal cards, or two pelts of the same kind";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();
        List<Card> drawnForThisEvent = HandUtil.drawEntireDeckIntoHand(player);
        List<Card> hand = player.getHand();
        List<int[]> matchingPairs = findMatchingAnimalPairs(hand);
        List<PeltKind> fusablePelts = fusablePeltKinds(player);

        if (matchingPairs.isEmpty() && fusablePelts.isEmpty()) {
            fallbackDuplicate(context, player, hand);
            HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
            return true;
        }

        List<String> labels = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        // Label by the pair's actual card descriptions, not just the shared name, so a
        // pair involving a previously-buffed card reads differently from a plain one.
        // Then deduplicate by that label text: if two different index-pairs would show
        // the exact same label, they're interchangeable (fusing either produces an
        // identical result) - offering both as separate options would just be two
        // pixel-identical choices with nothing to actually distinguish them.
        java.util.Set<String> seenLabels = new java.util.LinkedHashSet<>();
        for (int[] pair : matchingPairs) {
            String label = "Fuse " + hand.get(pair[0]) + " + " + hand.get(pair[1]);
            if (seenLabels.add(label)) {
                labels.add(label);
                actions.add(() -> fuseCards(context, player, pair));
            }
        }
        for (PeltKind kind : fusablePelts) {
            labels.add("Fuse two " + kind.getDisplayName() + "s");
            actions.add(() -> fusePelts(context, player, kind));
        }

        int choice = context.getUI().askChoice("Choose what to fuse:", labels);
        actions.get(choice).run();
        HandUtil.returnUnusedDrawnCards(player, drawnForThisEvent);
        return true;
    }

    private void fuseCards(RunContext context, Player player, int[] pair) {
        List<Card> hand = player.getHand();
        Card a = hand.get(pair[0]);
        Card b = hand.get(pair[1]);
        Card combined = Card.stitch(a, b);
        player.removeFromHand(a);
        player.removeFromHand(b);
        player.addToHand(combined);
        context.getUI().show("Combined into: " + combined);
    }

    private void fusePelts(RunContext context, Player player, PeltKind kind) {
        player.spendPelt(kind);
        player.spendPelt(kind);
        PeltKind fused = kind.fusedKind();
        player.gainPelt(fused);
        context.getUI().show("Fused into a " + fused.getDisplayName() + ".");
    }

    private void fallbackDuplicate(RunContext context, Player player, List<Card> hand) {
        List<Card> duplicatable = animalCardsWithSourceType(hand);
        if (duplicatable.isEmpty()) {
            context.getUI().show("Nothing to fuse, and nothing eligible to duplicate - nothing happens.");
            return;
        }
        Card original = duplicatable.get(random.nextInt(duplicatable.size()));
        Card duplicate = original.getSourceType().create();
        player.addToHand(duplicate);
        context.getUI().show("Nothing to fuse - the Mycologists grow you a copy of "
            + original.getName() + " instead: " + duplicate);
    }

    private List<PeltKind> fusablePeltKinds(Player player) {
        List<PeltKind> result = new ArrayList<>();
        for (PeltKind kind : BASE_PELT_KINDS) {
            if (player.getPeltCount(kind) >= 2) {
                result.add(kind);
            }
        }
        return result;
    }

    /** Every pair of hand positions sharing the same card name, excluding Squirrels entirely. */
    private List<int[]> findMatchingAnimalPairs(List<Card> hand) {
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).hasSigil("Squirrel")) {
                continue;
            }
            for (int j = i + 1; j < hand.size(); j++) {
                if (hand.get(j).hasSigil("Squirrel")) {
                    continue;
                }
                if (hand.get(i).getName().equals(hand.get(j).getName())) {
                    pairs.add(new int[]{i, j});
                }
            }
        }
        return pairs;
    }

    /** Animal (non-Squirrel) hand cards that can actually be duplicated - i.e. traceable to a real catalog entry. */
    private List<Card> animalCardsWithSourceType(List<Card> hand) {
        List<Card> eligible = new ArrayList<>();
        for (Card card : hand) {
            if (!card.hasSigil("Squirrel") && card.getSourceType() != null) {
                eligible.add(card);
            }
        }
        return eligible;
    }
}
