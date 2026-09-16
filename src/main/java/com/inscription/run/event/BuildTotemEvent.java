package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;
import com.inscription.sigil.RandomizableSigils;
import com.inscription.sigil.Sigil;
import com.inscription.totem.ActiveTotem;
import com.inscription.totem.TotemBody;
import com.inscription.totem.TotemHead;
import com.inscription.totem.TotemPiece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.inscription.util.GameRandom;
import java.util.Random;
import java.util.function.Supplier;

/**
 * The Woodcarver: offers 3 random totem pieces - a Head (for a tribe you
 * don't already hold a head for) or a Body (carrying a random sigil) - and
 * lets you add one to your inventory (capped at 6 pieces total, heads and
 * bodies combined). If you then hold at least one head and one body, you
 * may construct or reconfigure your single active totem from any pieces
 * currently in inventory - neither piece is consumed, so a later
 * Woodcarver visit can freely swap to a different combination.
 * <p>
 * An active totem's body-sigil is granted as a real, lasting ability to any
 * matching-tribe creature the moment it's placed on the board (see
 * GameEngine.applyActiveTotemIfMatching) - not a computed stat bonus.
 * <p>
 * If the inventory is already full when you visit, an Amalgam with a
 * random sigil is granted instead, matching the spirit of the original
 * "too many totem pieces" fallback.
 */
public class BuildTotemEvent implements NodeContent {

    private static final List<String> KNOWN_TRIBES = List.of("Avian", "Canine", "Hooved", "Insect", "Reptile");

    private final Random random;

    public BuildTotemEvent() {
        this(GameRandom.create());
    }

    /** Public so tests (including Main.java's own scenarios) can inject a seeded Random for deterministic outcomes. */
    public BuildTotemEvent(Random random) {
        this.random = random;
    }

    @Override
    public String describe() {
        return "The Woodcarver: collect a totem piece, then construct or reconfigure your active totem";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();
        showCurrentInventory(context, player);

        if (!player.hasRoomForTotemPiece()) {
            context.getUI().show("Your totem piece inventory is already full - the Woodcarver gives you an Amalgam instead.");
            Card amalgam = CardType.AMALGAM.create();
            amalgam.addSigil(RandomizableSigils.randomExcluding(random, amalgam));
            player.addToAnimalDeck(amalgam);
            context.getUI().show("Received: " + amalgam);
            return true;
        }

        List<TotemPiece> options = generateThreeOptions(player);
        List<String> optionTexts = options.stream().map(TotemPiece::describe).toList();
        int choice = context.getUI().askChoice("The Woodcarver offers you a choice of totem pieces:", optionTexts);
        TotemPiece chosen = options.get(choice);
        player.addTotemPiece(chosen);
        context.getUI().show("Added to your totem inventory: " + chosen.describe());

        offerConstructionIfEligible(context, player);
        return true;
    }

    /**
     * Shows what's already in the player's totem piece inventory before
     * offering anything new - matching how the Trader shows held pelts
     * upfront. Only shown here, at this event, since totem pieces are
     * otherwise invisible state the player has no other way to check.
     */
    private void showCurrentInventory(RunContext context, Player player) {
        List<TotemPiece> pieces = player.getTotemPieces();
        if (pieces.isEmpty()) {
            context.getUI().show("Your totem piece inventory is currently empty.");
        } else {
            String held = pieces.stream().map(TotemPiece::describe).reduce((a, b) -> a + ", " + b).orElse("");
            context.getUI().show("Your totem piece inventory (" + pieces.size() + "/" + Player.MAX_TOTEM_PIECES + "): " + held);
        }
    }

    private void offerConstructionIfEligible(RunContext context, Player player) {
        List<TotemHead> heads = player.getHeadsInInventory();
        List<TotemBody> bodies = player.getBodiesInInventory();
        if (heads.isEmpty() || bodies.isEmpty()) {
            return;
        }

        int doIt = context.getUI().askChoice(
            "You hold enough pieces to construct or reconfigure your active totem:",
            List.of("Skip", "Construct/reconfigure"));
        if (doIt == 0) {
            return;
        }

        List<String> headTexts = heads.stream().map(TotemHead::describe).toList();
        int headIndex = context.getUI().askChoice("Choose a head:", headTexts);

        List<String> bodyTexts = bodies.stream().map(TotemBody::describe).toList();
        int bodyIndex = context.getUI().askChoice("Choose a body:", bodyTexts);

        ActiveTotem totem = new ActiveTotem(heads.get(headIndex), bodies.get(bodyIndex));
        player.setActiveTotem(totem);
        context.getUI().show("Your active totem is now: " + totem);
    }

    /**
     * Builds a pool of every eligible piece - a Head for each tribe not
     * already held, a Body for each sigil not already held - then shuffles
     * and takes up to 3 distinct ones. Drawing from a pool this way (rather
     * than picking 3 times independently at random) is what guarantees the
     * offered set has no duplicates within itself and never repeats
     * anything already in the player's inventory - both were possible
     * before, since 3 independent random picks can easily land on the same
     * tribe or sigil twice, and nothing checked existing bodies at all.
     */
    private List<TotemPiece> generateThreeOptions(Player player) {
        List<TotemPiece> eligible = new ArrayList<>();

        for (String tribe : KNOWN_TRIBES) {
            if (!player.hasHeadForTribe(tribe)) {
                eligible.add(new TotemHead(tribe));
            }
        }

        List<String> heldBodySigilNames = player.getBodiesInInventory().stream()
            .map(TotemBody::getSigilName)
            .toList();
        for (Supplier<Sigil> sigilFactory : RandomizableSigils.POOL) {
            String sigilName = sigilFactory.get().getName();
            if (!heldBodySigilNames.contains(sigilName)) {
                eligible.add(new TotemBody(sigilFactory));
            }
        }

        Collections.shuffle(eligible, random);
        return eligible.subList(0, Math.min(3, eligible.size()));
    }
}
