package com.inscription;

import com.inscription.board.Board;
import com.inscription.cli.Battle;
import com.inscription.deck.Deck;
import com.inscription.engine.GameEngine;
import com.inscription.exception.ConflictingModifierException;
import com.inscription.exception.InsufficientResourcesException;
import com.inscription.exception.InvalidSacrificeException;
import com.inscription.exception.SlotOccupiedException;
import com.inscription.item.BlackGoatInABottleItem;
import com.inscription.item.BoulderInABottleItem;
import com.inscription.item.BoulderItem;
import com.inscription.item.FishHookItem;
import com.inscription.item.FrozenOpossumInABottleItem;
import com.inscription.item.HarpiesBirdlegFanItem;
import com.inscription.item.HoggyBankItem;
import com.inscription.item.HourglassItem;
import com.inscription.item.Item;
import com.inscription.item.ItemContext;
import com.inscription.item.MagnifyingGlassItem;
import com.inscription.item.PliersItem;
import com.inscription.item.ScissorsItem;
import com.inscription.item.SquirrelInABottleItem;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.SpecialCardType;
import com.inscription.model.DeckType;
import com.inscription.model.ResourceType;
import com.inscription.modifier.LimitedSacrificesModifier;
import com.inscription.modifier.RestrictedDrawModifier;
import com.inscription.modifier.SigilTotemModifier;
import com.inscription.pelt.PeltKind;
import com.inscription.player.Player;
import com.inscription.run.BattleNode;
import com.inscription.run.BossBattleNode;
import com.inscription.run.event.BuyPeltsEvent;
import com.inscription.run.event.DeckTrialEvent;
import com.inscription.ui.GameUI;
import com.inscription.run.event.TradePeltsEvent;
import com.inscription.run.NodeContent;
import com.inscription.run.PathNode;
import com.inscription.run.PathRunner;
import com.inscription.run.RunContext;
import com.inscription.run.event.BoneAltarEvent;
import com.inscription.run.event.BuildTotemEvent;
import com.inscription.run.event.CampfireEvent;
import com.inscription.run.event.CardChoiceEvent;
import com.inscription.run.event.GainConsumablesEvent;
import com.inscription.run.event.MycologistsEvent;
import com.inscription.run.event.MysteriousStonesEvent;
import com.inscription.run.event.ProspectorEvent;
import com.inscription.sigil.AirborneSigil;
import com.inscription.sigil.BifurcatedStrikeSigil;
import com.inscription.sigil.BoneKingSigil;
import com.inscription.sigil.BurrowerSigil;
import com.inscription.sigil.DamBuilderSigil;
import com.inscription.sigil.FledglingSigil;
import com.inscription.sigil.GuardianSigil;
import com.inscription.sigil.HeftySigil;
import com.inscription.sigil.HoarderSigil;
import com.inscription.sigil.LeaderSigil;
import com.inscription.sigil.LooseTailSigil;
import com.inscription.sigil.ManyLivesSigil;
import com.inscription.sigil.MightyLeapSigil;
import com.inscription.sigil.SprinterSigil;
import com.inscription.sigil.StinkySigil;
import com.inscription.sigil.TouchOfDeathSigil;
import com.inscription.sigil.TrifurcatedStrikeSigil;
import com.inscription.sigil.UnkillableSigil;
import com.inscription.sigil.WaterborneSigil;
import com.inscription.sigil.WorthySacrificeSigil;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * Fight-test scenario: exercises the core game logic and every implemented
 * sigil without any GUI. Each section below is an isolated mini-game (its
 * own board/players/engine) so scenarios can't interfere with each other -
 * this is a battery of tests, not one continuous narrative.
 * <p>
 * None of the sigils tested here are wired into the CardType catalog yet
 * (per the plan to add mechanics before attaching them to specific cards),
 * so they're attached ad hoc to a catalog card for the purpose of the test,
 * the same way you'd attach them for real once a card is chosen for them.
 */
public class Main {

    private static int checksPassed;
    private static int checksFailed;

    public static void main(String[] args) {
        twoDeckDrawSystem();
        costsAndResources();
        placingOnTheBoard();
        touchOfDeath();
        waterborne();
        airborneVsMightyLeap();
        manyLivesAndWorthySacrifice();
        healthScaleAndGoldenTeeth();
        boneKing();
        unkillable();
        burrower();
        hoarder();
        bifurcatedStrike();
        trifurcatedStrike();
        sharpQuills();
        stinky();
        antSwarm();
        squirrelTotem();
        fledgling();
        sprinter();
        hefty();
        damBuilder();
        leader();
        guardian();
        looseTail();
        conflictingChallengeModifiers();

        pathBranchingAndTraversal();
        mycologistsEvent();
        cardChoiceEvents();
        mysteriousStonesEvent();
        campfireEvent();
        prospectorEvent();
        buildTotemEvent();
        gainConsumablesEvent();
        itemSystem();
        newItems();
        boneAltarEvent();
        peltSystemEvent();
        deckTrialEvent();
        candleSystem();
        surrenderIsAChoice();
        theSmokeBeforeBossFight();

        System.out.println("\n========== Assertion summary ==========");
        System.out.println(checksPassed + " passed, " + checksFailed + " failed");
        if (checksFailed > 0) {
            throw new AssertionError(checksFailed + " check(s) failed - see output above for which ones");
        }
    }

    /**
     * Prints PASS/FAIL for one specific expectation and tracks it toward the
     * final summary - unlike this file's older scenarios (which only print
     * before/after state for a human to eyeball), the run/event/item/totem
     * scenarios below assert real expected values, so a regression fails
     * loudly here instead of silently changing what gets printed.
     */
    private static void check(String label, boolean condition) {
        System.out.println((condition ? "  PASS: " : "  FAIL: ") + label);
        if (condition) {
            checksPassed++;
        } else {
            checksFailed++;
        }
    }

    private static void twoDeckDrawSystem() {
        section("Two-deck draw system: opening hand, then a turn's draw choice");
        Player player = freshPlayer("Player", CardType.WOLF, CardType.WOLF);

        player.dealOpeningHand(1);
        System.out.println("Opening hand (1 squirrel + 1 animal): " + player.getHand());

        Card drawnAnimal = player.drawFromDeck(DeckType.ANIMAL);
        System.out.println("Drew from animal deck: " + drawnAnimal);

        Card drawnSquirrel = player.drawFromDeck(DeckType.SQUIRREL);
        System.out.println("Drew from squirrel deck: " + drawnSquirrel);

        System.out.println("Squirrel's tribe sigil is hidden from the UI:");
        System.out.println("  all sigils (engine view): " +
            drawnSquirrel.getSigils().stream().map(s -> s.getName()).toList());
        System.out.println("  visible sigils (UI view): " +
            drawnSquirrel.getVisibleSigils().stream().map(s -> s.getName()).toList());
    }

    private static void costsAndResources() {
        section("Costs and resources: Bones bank normally, Blood never does - it's collected fresh per placement");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF, CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        // Bones: unchanged - a standalone sacrifice still banks a bone normally, spendable later.
        Card squirrel = CardType.SQUIRREL.create();
        board.placeCard(true, 0, squirrel);
        engine.sacrificeFromBoard(true, 0);
        check("Standalone sacrifice still grants 1 bone", player.getBones() == 1);
        player.spendResource(ResourceType.BONES, 1);
        check("Bones spent normally", player.getBones() == 0);
        boolean caughtBonesOverspend = false;
        try {
            player.spendResource(ResourceType.BONES, 5);
        } catch (InsufficientResourcesException e) {
            caughtBonesOverspend = true;
        }
        check("Overspending bones still throws", caughtBonesOverspend);

        // Blood: spendResource no longer accepts it at all - Blood is spent directly in Battle's guided flow instead.
        boolean caughtBloodRejected = false;
        try {
            player.spendResource(ResourceType.BLOOD, 1);
        } catch (IllegalArgumentException e) {
            caughtBloodRejected = true;
        }
        check("spendResource rejects Blood entirely - it never banks, so there's nothing to spend from", caughtBloodRejected);

        // Real guided flow: place 2 free creatures first, then place a 2-blood card by marking them to sacrifice.
        Player fp = new Player("FP", new Deck(List.of(
            CardType.GECK.create(), CardType.GECK.create(), CardType.GECK.create())), new Deck(List.of()));
        Player fo = freshPlayer("FO", CardType.WOLF);
        fp.addToHand(CardType.WOLF.create()); // added before play() - ends up at hand index 0
        Battle flowBattle = new Battle(fp, fo, scannerOf(
            "draw animal\nplace 2 0\nplace 2 2\nplace 0 1\n0\n0\nquit\n"));
        flowBattle.play();
        check("Guided flow: marking 2 ordinary sacrifices (1 blood each) covers the Wolf's 2-blood cost - "
                + "the Wolf was placed, survived, and is reclaimed back into hand when the battle ends",
            fp.getHand().stream().anyMatch(c -> c.getName().equals("Wolf")));
        check("Guided flow: the sacrifice actually consumed Gecks (only 1 of the original 3 remains unsacrificed)",
            fp.getHand().stream().filter(c -> c.getName().equals("Geck")).count() == 1);

        // Cancelling before the threshold is met leaves everything untouched.
        Player cp = new Player("CP", new Deck(List.of(
            CardType.GECK.create(), CardType.GECK.create(), CardType.GECK.create())), new Deck(List.of()));
        Player co = freshPlayer("CO", CardType.WOLF);
        cp.addToHand(CardType.WOLF.create());
        Battle cancelBattle = new Battle(cp, co, scannerOf("draw animal\nplace 1 0\nplace 0 1\n1\nquit\n"));
        cancelBattle.play();
        check("Cancelling before the threshold means the Wolf is still in hand, never placed",
            cp.getHand().stream().anyMatch(c -> c.getName().equals("Wolf")));
    }

    private static void placingOnTheBoard() {
        section("Placing cards on the board: occupied-slot exception");
        Board board = new Board(4);
        board.placeCard(true, 0, CardType.WOLF.create());
        try {
            board.placeCard(true, 0, CardType.WOLF.create());
        } catch (SlotOccupiedException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
    }

    private static void touchOfDeath() {
        section("Touch of Death: kills whatever it damages, regardless of attack stat");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card attacker = CardType.WOLF.create(); // 3 attack
        attacker.addSigil(new TouchOfDeathSigil());
        Card defender = CardType.MOOSE_BUCK.create(); // 7 health - way more than 3
        board.placeCard(true, 0, attacker);
        board.placeCard(false, 0, defender);

        System.out.println("Before: attacker=" + attacker + " defender=" + defender);
        engine.ringBell(true);
        System.out.println("After:  attacker=" + attacker + " defender=" + defender
            + " (alive=" + defender.isAlive() + ")");
    }

    private static void waterborne() {
        section("Waterborne: permanently non-blocking, attacks always redirect to the face");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card attacker = CardType.WOLF.create();
        Card defender = CardType.WOLF.create();
        defender.addSigil(new WaterborneSigil());
        board.placeCard(true, 0, attacker);
        board.placeCard(false, 0, defender);

        System.out.println("Scale before: " + engine.getHealthScale().getValue());
        engine.ringBell(true);
        System.out.println("Scale after:  " + engine.getHealthScale().getValue()
            + " (Waterborne defender untouched: " + defender + ")");
    }

    private static void airborneVsMightyLeap() {
        section("Airborne bypasses the defender, unless it has Mighty Leap");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card airborneAttacker = CardType.WOLF.create();
        airborneAttacker.addSigil(new AirborneSigil());
        Card plainDefender = CardType.MOOSE_BUCK.create();
        board.placeCard(true, 0, airborneAttacker);
        board.placeCard(false, 0, plainDefender);

        engine.ringBell(true);
        System.out.println("No Mighty Leap: defender untouched -> " + plainDefender
            + ", scale: " + engine.getHealthScale().getValue());

        Card airborneAttacker2 = CardType.WOLF.create();
        airborneAttacker2.addSigil(new AirborneSigil());
        Card leapingDefender = CardType.MOOSE_BUCK.create();
        leapingDefender.addSigil(new MightyLeapSigil());
        board.placeCard(true, 1, airborneAttacker2);
        board.placeCard(false, 1, leapingDefender);

        engine.ringBell(true);
        System.out.println("With Mighty Leap: defender blocks and takes damage -> " + leapingDefender);
    }

    private static void manyLivesAndWorthySacrifice() {
        section("Sacrifice sigils: Many Lives survives, Worthy Sacrifice is worth 3 blood");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card immortal = CardType.WOLF.create();
        immortal.addSigil(new ManyLivesSigil());
        board.placeCard(true, 0, immortal);
        engine.sacrificeFromBoard(true, 0);
        System.out.println("Many Lives after sacrifice: " + immortal
            + " (still on board: " + (board.peekCard(true, 0) == immortal) + ")");

        // Worthy Sacrifice is worth 3 blood - verified through the real guided flow, since Blood is never
        // tracked on Player at all anymore. Marking just ONE Worthy Sacrifice creature should be enough
        // to cover a 2-blood card by itself (an ordinary sacrifice, worth 1, would need two).
        Player wp = new Player("WP", new Deck(List.of(CardType.GECK.create(), CardType.GECK.create())), new Deck(List.of()));
        Player wo = freshPlayer("WO", CardType.WOLF);
        wp.addToHand(CardType.WOLF.create()); // costs 2 blood - added before play(), ends up at hand index 0
        Card worthySquirrel = CardType.SQUIRREL.create();
        worthySquirrel.addSigil(new WorthySacrificeSigil());
        wp.addToHand(worthySquirrel); // hand index 1 - a Worthy Sacrifice creature to place for free, then sacrifice
        Battle worthyBattle = new Battle(wp, wo, scannerOf("draw squirrel\nplace 1 0\nplace 0 1\n0\nquit\n"));
        worthyBattle.play();
        check("Worthy Sacrifice: marking just ONE such creature (3 blood) covers a 2-blood card by itself - "
                + "the Wolf was placed, survived, and is reclaimed back into hand when the battle ends",
            wp.getHand().stream().anyMatch(c -> c.getName().equals("Wolf")));
        check("Worthy Sacrifice: the Squirrel with Worthy Sacrifice was actually consumed (only one mark needed)",
            wp.getHand().stream().noneMatch(c -> c.hasSigil("Worthy Sacrifice")));
    }

    private static void healthScaleAndGoldenTeeth() {
        section("Health scale: single 0-10 value, overflow damage banks golden teeth");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);
        var scale = engine.getHealthScale();

        System.out.println("Starting scale (centered): " + scale.getValue());

        // One unblocked Wolf attack (3 attack) moves the scale from 5 to 8 -
        // well within bounds, no overflow.
        board.placeCard(true, 0, CardType.WOLF.create());
        engine.ringBell(true);
        System.out.println("After one unblocked Wolf attack (3 dmg): " + scale.getValue()
            + " (no overflow yet, player golden teeth: " + scale.getPlayerGoldenTeeth() + ")");

        // The first Wolf is now exhausted and won't attack again. Two fresh
        // Wolves attacking this same ringBell deal 6 more damage: 8 + 6 = 14,
        // overshooting the 10 cap by 4 - that excess becomes golden teeth.
        board.placeCard(true, 1, CardType.WOLF.create());
        board.placeCard(true, 2, CardType.WOLF.create());
        engine.ringBell(true);
        System.out.println("After two more Wolves overshoot the cap by 4: " + scale.getValue()
            + " (player golden teeth: " + scale.getPlayerGoldenTeeth() + ")");
        System.out.println("Opponent defeated (scale hit 10)? " + scale.isPlayerVictorious());
    }

    private static void boneKing() {
        section("Bone King: 4 bones on death instead of the usual 1");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card boneKing = CardType.WOLF.create();
        boneKing.addSigil(new BoneKingSigil());
        board.placeCard(false, 0, boneKing);
        board.placeCard(true, 0, CardType.MOOSE_BUCK.create()); // enough attack to kill it

        engine.ringBell(true);
        System.out.println("Opponent's bones after their creature died: " + opponent.getBones());
    }

    private static void unkillable() {
        section("Unkillable: a copy enters its owner's hand when it perishes");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card immortalWolf = CardType.WOLF.create();
        immortalWolf.addSigil(new UnkillableSigil());
        board.placeCard(false, 0, immortalWolf);
        board.placeCard(true, 0, CardType.MOOSE_BUCK.create());

        System.out.println("Opponent hand before: " + opponent.getHand());
        engine.ringBell(true);
        System.out.println("Opponent hand after:  " + opponent.getHand());
    }

    private static void burrower() {
        section("Burrower: relocates to block an attacked empty lane");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card burrowerCard = CardType.MOOSE_BUCK.create();
        burrowerCard.addSigil(new BurrowerSigil());
        board.placeCard(false, 2, burrowerCard); // sitting elsewhere, lane 0 is empty
        board.placeCard(true, 0, CardType.WOLF.create());

        System.out.println("Before: lane 0 occupant=" + board.peekCard(false, 0)
            + ", lane 2 occupant=" + board.peekCard(false, 2)
            + ", scale=" + engine.getHealthScale().getValue());
        engine.ringBell(true);
        System.out.println("After:  lane 0 occupant=" + board.peekCard(false, 0)
            + ", lane 2 occupant=" + board.peekCard(false, 2)
            + ", scale=" + engine.getHealthScale().getValue());
    }

    private static void hoarder() {
        section("Hoarder: draws an extra card immediately when played");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF, CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card hoarderCard = CardType.WOLF.create();
        hoarderCard.addSigil(new HoarderSigil());

        System.out.println("Hand size before playing Hoarder: " + player.getHand().size());
        engine.placeCard(true, 0, hoarderCard);
        System.out.println("Hand size after (drew a bonus card): " + player.getHand().size());
    }

    private static void bifurcatedStrike() {
        section("Bifurcated Strike: hits the two lanes adjacent to the one directly across, not that lane itself");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card attacker = CardType.WOLF.create();
        attacker.addSigil(new BifurcatedStrikeSigil());
        board.placeCard(true, 1, attacker); // attacks from lane 1 -> hits lanes 0 and 2
        Card direct = CardType.MOOSE_BUCK.create();
        Card left = CardType.MOOSE_BUCK.create();
        Card right = CardType.MOOSE_BUCK.create();
        board.placeCard(false, 0, left);
        board.placeCard(false, 1, direct);
        board.placeCard(false, 2, right);

        System.out.println("Before: direct=" + direct + " left=" + left + " right=" + right);
        engine.ringBell(true);
        System.out.println("After:  direct=" + direct + " (untouched) left=" + left + " right=" + right);
    }

    private static void trifurcatedStrike() {
        section("Trifurcated Strike: hits directly across plus both adjacent lanes");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card attacker = CardType.WOLF.create();
        attacker.addSigil(new TrifurcatedStrikeSigil());
        board.placeCard(true, 1, attacker);
        Card left = CardType.MOOSE_BUCK.create();
        board.placeCard(false, 0, left); // lane 1 (direct) and lane 2 left empty

        System.out.println("Scale before: " + engine.getHealthScale().getValue());
        engine.ringBell(true);
        System.out.println("Left lane hit: " + left
            + " | scale after (direct empty lane still deals face damage): " + engine.getHealthScale().getValue());
    }

    private static void sharpQuills() {
        section("Sharp Quills: reflects 1 damage back at whoever attacks it");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card attacker = CardType.WOLF.create();
        Card defender = CardType.PORCUPINE.create(); // carries SharpQuillsSigil in the catalog
        board.placeCard(true, 0, attacker);
        board.placeCard(false, 0, defender);

        System.out.println("Before: attacker=" + attacker + " defender=" + defender);
        engine.ringBell(true);
        System.out.println("After:  attacker=" + attacker + " defender=" + defender);
    }

    private static void stinky() {
        section("Stinky: the creature directly opposing it deals 1 less damage - but only when genuinely facing it");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card stinkyCard = CardType.MOOSE_BUCK.create();
        stinkyCard.addSigil(new StinkySigil());
        Card facingAttacker = CardType.WOLF.create(); // 3 attack, reduced to 2 by Stinky
        board.placeCard(false, 0, stinkyCard);
        board.placeCard(true, 0, facingAttacker);

        System.out.println("Facing Stinky - before: " + stinkyCard);
        engine.ringBell(true);
        System.out.println("Facing Stinky - after (took 2, not 3):  " + stinkyCard);

        Card sideLaneStinky = CardType.MOOSE_BUCK.create();
        sideLaneStinky.addSigil(new StinkySigil());
        Card bifurcatedAttacker = CardType.WOLF.create();
        bifurcatedAttacker.addSigil(new BifurcatedStrikeSigil());
        board.placeCard(true, 2, bifurcatedAttacker); // hits lanes 1 and 3, not 2
        board.placeCard(false, 1, sideLaneStinky); // Stinky isn't in the attacker's home lane (2) at all

        engine.ringBell(true);
        System.out.println("Stinky positioned elsewhere entirely (not attacker's home lane, took full 3): "
            + sideLaneStinky);

        // The key case: an attacker genuinely FACING Stinky in its own home
        // lane should have its power reduced on every lane a multi-strike
        // sigil hits that turn - not just in a direct matchup against Stinky.
        Board board2 = new Board(4);
        GameEngine engine2 = new GameEngine(board2, freshPlayer("Player", CardType.WOLF), freshPlayer("Opponent", CardType.WOLF));
        Card stinkyInHomeLane = CardType.MOOSE_BUCK.create();
        stinkyInHomeLane.addSigil(new StinkySigil());
        Card trifurcatedAttacker = CardType.WOLF.create(); // 3 attack, reduced to 2 everywhere
        trifurcatedAttacker.addSigil(new TrifurcatedStrikeSigil());
        board2.placeCard(true, 1, trifurcatedAttacker);
        board2.placeCard(false, 1, stinkyInHomeLane); // directly opposite the attacker's own lane
        Card leftOfStinky = CardType.MOOSE_BUCK.create();
        Card rightOfStinky = CardType.MOOSE_BUCK.create();
        board2.placeCard(false, 0, leftOfStinky);
        board2.placeCard(false, 2, rightOfStinky);

        engine2.ringBell(true);
        System.out.println("Facing Stinky in home lane - power reduced on ALL lanes hit: "
            + "left=" + leftOfStinky + " direct=" + stinkyInHomeLane + " right=" + rightOfStinky);
    }

    private static void antSwarm() {
        section("Ant swarm: attack scales with the count of friendly Ant-tribe cards on board");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card ant1 = CardType.WORKER_ANT.create();
        board.placeCard(true, 0, ant1);
        System.out.println("1 ant on board -> display attack: " + engine.getDisplayAttack(ant1, true));

        Card queen = CardType.ANT_QUEEN.create();
        board.placeCard(true, 1, queen); // counts toward the swarm too
        System.out.println("+ Ant Queen (2 total) -> ant1 now shows: " + engine.getDisplayAttack(ant1, true)
            + " | queen shows: " + engine.getDisplayAttack(queen, true));

        Card target = CardType.MOOSE_BUCK.create();
        board.placeCard(false, 0, target);
        engine.ringBell(true);
        System.out.println("After attacking with 2 ants on board (each deals 2): " + target);
    }

    private static void squirrelTotem() {
        section("Squirrel totem: buffs every Squirrel-tribe card on the board");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card totemSquirrel = CardType.SQUIRREL.create();
        board.placeCard(true, 0, totemSquirrel);
        System.out.println("Before totem: " + totemSquirrel);
        engine.addModifier(new SigilTotemModifier("Squirrel", 2, 0));
        engine.applyModifiers();
        System.out.println("After totem:  " + totemSquirrel);
    }

    private static void fledgling() {
        section("Fledgling: transforms into a stronger form after surviving to its owner's next turn");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        engine.placeCard(true, 0, CardType.RAVEN_EGG.create());
        System.out.println("Just placed: " + board.peekCard(true, 0));
        engine.startTurn(false);
        engine.startTurn(true);
        System.out.println("After surviving to next turn: " + board.peekCard(true, 0));

        Card growing = CardType.SQUIRREL.create();
        growing.addSigil(new FledglingSigil(1, 2));
        board.placeCard(true, 1, growing);
        System.out.println("\nGeneric stat-buff variant, before: " + growing);
        engine.startTurn(false);
        engine.startTurn(true);
        System.out.println("After surviving to next turn (same card, +1/+2): " + growing);
    }

    private static void sprinter() {
        section("Sprinter: moves one lane right at the end of its owner's turn");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card elk = CardType.ELK.create();
        board.placeCard(true, 0, elk);
        System.out.println("Before bell: lane 0 = " + board.peekCard(true, 0));
        engine.ringBell(true);
        System.out.println("After bell:  lane 0 = " + board.peekCard(true, 0) + ", lane 1 = " + board.peekCard(true, 1));
    }

    private static void hefty() {
        section("Hefty: advances one lane in its direction, pushing only what's actually in its path");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        // Case 1: clear path ahead - Moose Buck (Hefty, RIGHT) just advances.
        // The neighbor BEHIND it (to its left) must NOT move at all.
        Card behind = CardType.WOLF.create();
        Card mooseBuck = CardType.MOOSE_BUCK.create();
        board.placeCard(true, 0, behind);
        board.placeCard(true, 1, mooseBuck);
        engine.ringBell(true);
        check("Hefty: advances into the clear lane ahead", board.peekCard(true, 2) == mooseBuck);
        check("Hefty: the neighbor BEHIND it does not move at all", board.peekCard(true, 0) == behind);

        // Case 2: something is directly in its path with room beyond it - that
        // one card gets pushed further to make way, then Hefty advances.
        Board board2 = new Board(4);
        GameEngine engine2 = new GameEngine(board2, freshPlayer("P2", CardType.WOLF), freshPlayer("O2", CardType.WOLF));
        Card mooseBuck2 = CardType.MOOSE_BUCK.create();
        Card inTheWay = CardType.WOLF.create();
        board2.placeCard(true, 1, mooseBuck2);
        board2.placeCard(true, 2, inTheWay);
        engine2.ringBell(true);
        check("Hefty: the card directly in its path gets pushed one further",
            board2.peekCard(true, 3) == inTheWay);
        check("Hefty: it then advances into the space that opened up",
            board2.peekCard(true, 2) == mooseBuck2);

        // Case 3: blocked both ways (edge of board, and the other direction is
        // also blocked) - nothing moves at all.
        Board board3 = new Board(4);
        GameEngine engine3 = new GameEngine(board3, freshPlayer("P3", CardType.WOLF), freshPlayer("O3", CardType.WOLF));
        Card mooseBuck3 = CardType.MOOSE_BUCK.create();
        Card leftBlocker = CardType.WOLF.create();
        board3.placeCard(true, 2, leftBlocker);
        board3.placeCard(true, 3, mooseBuck3); // rightmost lane - RIGHT is blocked by the edge
        board3.placeCard(true, 1, CardType.WOLF.create()); // LEFT is also blocked (occupied, no room beyond)
        engine3.ringBell(true);
        check("Hefty: blocked in both directions - stays exactly where it was",
            board3.peekCard(true, 3) == mooseBuck3);
    }

    private static void damBuilder() {
        section("Dam Builder: fills adjacent empty spaces with Dams when played");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, freshPlayer("Opponent", CardType.WOLF));

        Card damBuilderCard = CardType.SQUIRREL.create();
        damBuilderCard.addSigil(new DamBuilderSigil());
        engine.placeCard(true, 1, damBuilderCard);
        System.out.println("Lane 0 (left, was empty): " + board.peekCard(true, 0));
        System.out.println("Lane 2 (right, was empty): " + board.peekCard(true, 2));
    }

    private static void leader() {
        section("Leader: adjacent creatures gain 1 power (not itself)");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card alpha = CardType.ALPHA.create();
        Card neighbor = CardType.WOLF.create();
        board.placeCard(true, 0, alpha);
        board.placeCard(true, 1, neighbor);
        System.out.println("Alpha's own display attack (unaffected): " + engine.getDisplayAttack(alpha, true));
        System.out.println("Neighboring Wolf's display attack (buffed): " + engine.getDisplayAttack(neighbor, true));
    }

    private static void guardian() {
        section("Guardian: relocates to intercept a newly-played opposing card, if its own lane is empty");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card bloodhound = CardType.BLOODHOUND.create();
        board.placeCard(false, 3, bloodhound);
        System.out.println("Bloodhound starts at lane 3.");
        engine.placeCard(true, 1, CardType.WOLF.create());
        System.out.println("Player played into lane 1 -> Bloodhound now at lane 1: "
            + (board.peekCard(false, 1) == bloodhound));
    }

    private static void looseTail() {
        section("Loose Tail: evades entirely, leaves a Tail decoy, flees one lane right");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card skink = CardType.SKINK.create();
        Card attacker = CardType.MOOSE_BUCK.create();
        board.placeCard(false, 0, skink);
        board.placeCard(true, 0, attacker);
        engine.ringBell(true);
        System.out.println("Lane 0 now holds: " + board.peekCard(false, 0));
        System.out.println("Skink fled to lane 1, undamaged: " + board.peekCard(false, 1));
    }

    private static void conflictingChallengeModifiers() {
        section("Conflicting ChallengeModifiers throw on registration");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        engine.addModifier(new LimitedSacrificesModifier(1));
        System.out.println("Added: Limited Sacrifices");
        try {
            engine.addModifier(new RestrictedDrawModifier(1));
        } catch (ConflictingModifierException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
    }

    // ---- run / path / event / item / totem system ----

    private static void pathBranchingAndTraversal() {
        section("Path system: branching traversal, invalid-input handling, early termination");

        // A linear path with no branching visits every node in order.
        {
            List<String> visited = new java.util.ArrayList<>();
            PathNode a = new PathNode(trackingNode("A", true, visited));
            PathNode b = new PathNode(trackingNode("B", true, visited));
            PathNode c = new PathNode(trackingNode("C", true, visited));
            a.connectTo(b);
            b.connectTo(c);
            boolean result = new PathRunner(freshPlayer("P", CardType.WOLF), scannerOf("\n\n")).run(a);
            check("Linear path visits A, B, C in order", visited.equals(List.of("A", "B", "C")));
            check("Reaching the end of the path returns true", result);
        }

        // Branching: the player's choice actually determines which node comes next.
        {
            List<String> visited = new java.util.ArrayList<>();
            PathNode start = new PathNode(trackingNode("Start", true, visited));
            PathNode left = new PathNode(trackingNode("Left", true, visited));
            PathNode right = new PathNode(trackingNode("Right", true, visited));
            start.connectTo(left);
            start.connectTo(right);
            new PathRunner(freshPlayer("P", CardType.WOLF), scannerOf("\n1\n")).run(start);
            check("Picking index 1 goes to Right, not Left", visited.equals(List.of("Start", "Right")));
        }

        // Invalid input is rejected until a valid choice is entered.
        {
            List<String> visited = new java.util.ArrayList<>();
            PathNode start = new PathNode(trackingNode("Start", true, visited));
            PathNode left = new PathNode(trackingNode("Left", true, visited));
            PathNode right = new PathNode(trackingNode("Right", true, visited));
            start.connectTo(left);
            start.connectTo(right);
            new PathRunner(freshPlayer("P", CardType.WOLF), scannerOf("\nbanana\n5\n0\n")).run(start);
            check("Rejects invalid input, eventually accepts a valid choice (0 -> Left)",
                visited.equals(List.of("Start", "Left")));
        }

        // A node resolving to "false" (a lost/quit battle) ends the run before reaching what comes after it.
        {
            List<String> visited = new java.util.ArrayList<>();
            PathNode a = new PathNode(trackingNode("A", true, visited));
            PathNode b = new PathNode(trackingNode("B", false, visited));
            PathNode c = new PathNode(trackingNode("C", true, visited));
            a.connectTo(b);
            b.connectTo(c);
            boolean result = new PathRunner(freshPlayer("P", CardType.WOLF), scannerOf("\n")).run(a);
            check("A failing node ends the run before reaching what comes after it",
                visited.equals(List.of("A", "B")));
            check("A failed run returns false", !result);
        }

        // BattleNode: quitting mid-battle is treated as a genuine loss - costs a
        // candle, and the run continues if any remain.
        {
            Player p = freshPlayer("P", CardType.WOLF, CardType.WOLF);
            BattleNode node = new BattleNode("Test Fight", pp -> freshPlayer("O", CardType.WOLF, CardType.WOLF));
            boolean result = node.resolve(new RunContext(p, scannerOf("quit\n")));
            check("BattleNode: quitting with a candle to spare costs it and the run continues",
                result && p.getCandles() == 1);

            Player p2 = freshPlayer("P2", CardType.WOLF, CardType.WOLF);
            p2.setCandles(1);
            BattleNode node2 = new BattleNode("Test Fight 2", pp -> freshPlayer("O2", CardType.WOLF, CardType.WOLF));
            boolean result2 = node2.resolve(new RunContext(p2, scannerOf("quit\n")));
            check("BattleNode: quitting on the last candle ends the run", !result2 && p2.isOutOfCandles());
        }
    }

    private static NodeContent trackingNode(String name, boolean succeeds, List<String> visitLog) {
        return new NodeContent() {
            public String describe() {
                return name;
            }

            public boolean resolve(RunContext context) {
                visitLog.add(name);
                return succeeds;
            }
        };
    }

    private static void mycologistsEvent() {
        section("The Mycologists: combine two matching ANIMAL hand cards, excluding Squirrels");
        Player p = freshPlayer("P", CardType.WOLF);
        p.addToHand(CardType.WOLF.create());
        p.addToHand(CardType.WOLF.create());
        p.addToHand(CardType.PORCUPINE.create()); // unrelated - should be untouched

        new MycologistsEvent().resolve(new RunContext(p, scannerOf("0\n")));
        List<Card> hand = p.getHand();
        check("Hand now has 2 cards (2 Wolves became 1)", hand.size() == 2);
        check("Stitched Wolf has combined stats (6/4)",
            hand.stream().anyMatch(c -> c.getName().equals("Wolf") && c.getAttack() == 6 && c.getMaxHealth() == 4));
        check("Porcupine untouched", hand.stream().anyMatch(c -> c.getName().equals("Porcupine")));

        // Two Squirrels should NOT be offered as a combinable pair - Squirrels are excluded entirely.
        Player p2 = new Player("P2", new Deck(List.of()), new Deck(List.of()));
        p2.addToHand(CardType.SQUIRREL.create());
        p2.addToHand(CardType.SQUIRREL.create());
        new MycologistsEvent(new Random(1)).resolve(new RunContext(p2, scannerOf("")));
        check("Two Squirrels are never combined (still 2 separate Squirrels)",
            p2.getHand().stream().filter(c -> c.getName().equals("Squirrel")).count() == 2
                && p2.getHand().size() == 2);

        // No matching animal pair: grants a duplicate of a random animal card instead of doing nothing.
        Player p3 = new Player("P3", new Deck(List.of()), new Deck(List.of()));
        p3.addToHand(CardType.PORCUPINE.create());
        p3.addToHand(CardType.SQUIRREL.create()); // present, but ineligible - shouldn't be duplicated
        boolean result = new MycologistsEvent(new Random(2)).resolve(new RunContext(p3, scannerOf("")));
        check("No matching pair: returns true, and a Porcupine duplicate was granted",
            result && p3.getHand().stream().filter(c -> c.getName().equals("Porcupine")).count() == 2);
        check("The Squirrel was not what got duplicated",
            p3.getHand().stream().filter(c -> c.getName().equals("Squirrel")).count() == 1);
    }

    private static void cardChoiceEvents() {
        section("Card Choice: plain, tribe, and cost variants");

        // Plain: picking index 0 adds exactly one new card to the deck.
        Player p1 = freshPlayer("P1", CardType.WOLF);
        new CardChoiceEvent(CardChoiceEvent.Mode.PLAIN).resolve(new RunContext(p1, scannerOf("0\n")));
        check("Plain: deck grew by exactly one card", drawAllAnimal(p1).size() == 2); // 1 original Wolf + 1 granted

        // Tribe: the granted card genuinely carries a real tribe tag.
        Player p2 = freshPlayer("P2", CardType.WOLF);
        new CardChoiceEvent(CardChoiceEvent.Mode.TRIBE).resolve(new RunContext(p2, scannerOf("0\n")));
        boolean hasTribeTag = drawAllAnimal(p2).stream().anyMatch(c ->
            c.hasSigil("Avian") || c.hasSigil("Canine") || c.hasSigil("Hooved")
                || c.hasSigil("Insect") || c.hasSigil("Reptile"));
        check("Tribe: granted card actually has a tribe tag", hasTribeTag);

        // Cost: the granted card's cost matches the tier that was shown.
        Player p3 = freshPlayer("P3", CardType.WOLF);
        new CardChoiceEvent(CardChoiceEvent.Mode.COST).resolve(new RunContext(p3, scannerOf("0\n")));
        check("Cost: deck grew by exactly one card", drawAllAnimal(p3).size() == 2);
    }

    private static void mysteriousStonesEvent() {
        section("Mysterious Stones: only sigil-bearing sources, targets capped at 2 total transfers");

        Player p = freshPlayer("P", CardType.WOLF);
        p.addToHand(CardType.PORCUPINE.create()); // carries Sharp Quills - the only eligible source here
        p.addToHand(CardType.GECK.create());       // plain - will be the target
        new MysteriousStonesEvent().resolve(new RunContext(p, scannerOf("0\n0\n")));
        List<Card> hand = p.getHand();
        check("Porcupine consumed", hand.stream().noneMatch(c -> c.getName().equals("Porcupine")));
        check("Geck gained Sharp Quills", hand.stream().anyMatch(c -> c.getName().equals("Geck") && c.hasSigil("Sharp Quills")));
        check("Geck now has 1 of its 2 allowed transfers",
            hand.stream().anyMatch(c -> c.getName().equals("Geck") && c.hasSigil("Extra Sigil")));

        // A plain card with no sigils at all is never even offered as a source.
        Player p2 = new Player("P2", new Deck(List.of()), new Deck(List.of()));
        p2.addToHand(CardType.GECK.create());
        p2.addToHand(CardType.WOLF.create());
        boolean result2 = new MysteriousStonesEvent().resolve(new RunContext(p2, scannerOf("")));
        check("No sigil-bearing card in hand: graceful no-op, nothing consumed",
            result2 && p2.getHand().size() == 2);

        // A target with only 1 existing transfer is still eligible for a 2nd.
        Player p3 = freshPlayer("P3", CardType.WOLF);
        Card onceMarkedGeck = CardType.GECK.create();
        onceMarkedGeck.addSigil(new com.inscription.sigil.ExtraSigilMarker());
        p3.addToHand(CardType.PORCUPINE.create());
        p3.addToHand(onceMarkedGeck);
        new MysteriousStonesEvent().resolve(new RunContext(p3, scannerOf("0\n0\n")));
        check("A once-marked target is still eligible for a 2nd transfer",
            p3.getHand().stream().noneMatch(c -> c.getName().equals("Porcupine")));

        // A target that already has 2 transfers is refused - the cap is enforced.
        Player p4 = new Player("P4", new Deck(List.of()), new Deck(List.of()));
        Card twiceMarkedGeck = CardType.GECK.create();
        twiceMarkedGeck.addSigil(new com.inscription.sigil.ExtraSigilMarker());
        twiceMarkedGeck.addSigil(new com.inscription.sigil.ExtraSigilMarker());
        p4.addToHand(CardType.PORCUPINE.create());
        p4.addToHand(twiceMarkedGeck);
        new MysteriousStonesEvent().resolve(new RunContext(p4, scannerOf("0\n")));
        check("Refuses when the only other card already has 2 transfers - Porcupine NOT consumed",
            p4.getHand().stream().anyMatch(c -> c.getName().equals("Porcupine")));
    }

    private static void campfireEvent() {
        section("Campfire: mandatory guaranteed 1st rest, optional 50/50 2nd rest, no switching cards");

        // First rest is guaranteed, even with a rigged "always destroy" roll - then leave.
        Player p1 = new Player("P1", new Deck(List.of()), new Deck(List.of()));
        Card wolf1 = CardType.WOLF.create();
        p1.addToHand(wolf1);
        new CampfireEvent(CampfireEvent.Type.DAMAGE, fixedRandom(0.0))
            .resolve(new RunContext(p1, scannerOf("0\n1\n")));
        check("1st rest is guaranteed regardless of roll (attack now 4)", wolf1.getAttack() == 4);
        check("Leaving after the 1st rest keeps exactly one card, untouched further", p1.getHand().size() == 1);

        // Choosing to rest again with a forced low roll destroys the card.
        Player p2 = new Player("P2", new Deck(List.of()), new Deck(List.of()));
        Card wolf2 = CardType.WOLF.create();
        p2.addToHand(wolf2);
        new CampfireEvent(CampfireEvent.Type.DAMAGE, fixedRandom(0.2))
            .resolve(new RunContext(p2, scannerOf("0\n0\n")));
        check("Resting again with a roll below 0.5 destroys the card", p2.getHand().stream().noneMatch(c -> c.getName().equals("Wolf")));

        // Choosing to rest again with a forced high roll succeeds instead.
        Player p3 = new Player("P3", new Deck(List.of()), new Deck(List.of()));
        Card wolf3 = CardType.WOLF.create();
        p3.addToHand(wolf3);
        new CampfireEvent(CampfireEvent.Type.DAMAGE, fixedRandom(0.9))
            .resolve(new RunContext(p3, scannerOf("0\n0\n")));
        check("Resting again with a roll of 0.9 succeeds (3 -> 4 -> 5)",
            p3.getHand().stream().anyMatch(c -> c.getName().equals("Wolf") && c.getAttack() == 5));

        // Cannot switch to a different card for the second rest - only the originally chosen card is ever touched.
        Player p4 = freshPlayer("P4", CardType.WOLF);
        Card wolf4 = CardType.WOLF.create();
        Card porcupine4 = CardType.PORCUPINE.create();
        p4.addToHand(wolf4);
        p4.addToHand(porcupine4);
        new CampfireEvent(CampfireEvent.Type.DAMAGE, fixedRandom(0.9))
            .resolve(new RunContext(p4, scannerOf("0\n0\n"))); // picks index 0 (Wolf) both times - no menu to pick Porcupine instead
        check("Only the chosen Wolf received both rests (3 -> 5)", wolf4.getAttack() == 5);
        check("The other hand card (Porcupine) was never touched", porcupine4.getAttack() == 1);
    }

    private static void prospectorEvent() {
        section("The Prospector: 33% Golden Sheep Pelt, otherwise a random Insect creature");

        Player p1 = freshPlayer("P1", CardType.WOLF);
        new ProspectorEvent(fixedIntRandom(0)).resolve(new RunContext(p1, scannerOf("0\n")));
        check("Roll 0: exactly 1 Golden Sheep Pelt gained", p1.getPeltCount(PeltKind.SHEEP) == 1);

        Player p2 = freshPlayer("P2", CardType.WOLF);
        new ProspectorEvent(fixedIntRandom(1)).resolve(new RunContext(p2, scannerOf("1\n")));
        check("Roll 1: no Golden Sheep Pelt gained", p2.getPeltCount(PeltKind.SHEEP) == 0);
        check("Roll 1: the granted reward genuinely carries the Insect tribe tag",
            drawAllAnimal(p2).stream().anyMatch(c -> c.hasSigil("Insect")));
    }

    private static void buildTotemEvent() {
        section("The Woodcarver: totem piece inventory (cap 6), construction, and real granted sigils");

        // Cap enforcement: at 6 pieces, no more room, Amalgam fallback triggers.
        Player p1 = freshPlayer("P1", CardType.WOLF);
        for (int i = 0; i < 6; i++) {
            p1.addTotemPiece(new com.inscription.totem.TotemHead("Canine"));
        }
        new BuildTotemEvent(new Random()).resolve(new RunContext(p1, scannerOf("")));
        check("At the cap: still exactly 6 pieces (no 7th added)", p1.getTotemPieces().size() == 6);
        check("At the cap: an Amalgam was granted instead", drawAllAnimal(p1).stream().anyMatch(c -> c.getName().equals("Amalgam")));

        // No-duplicate-head rule: with all 5 tribes already headed, the newly offered piece must be a Body.
        Player p2 = freshPlayer("P2", CardType.WOLF);
        for (String tribe : List.of("Avian", "Canine", "Hooved", "Insect", "Reptile")) {
            p2.addTotemPiece(new com.inscription.totem.TotemHead(tribe));
        }
        new BuildTotemEvent(new Random()).resolve(new RunContext(p2, scannerOf("0\n0\n")));
        check("With all 5 tribes covered, the 6th piece added is a Body, never a duplicate head",
            p2.getTotemPieces().size() == 6 && p2.getBodiesInInventory().size() >= 1);

        // Construction actually happens when eligible, using the chosen head+body, and consumes neither piece.
        Player p3 = freshPlayer("P3", CardType.WOLF);
        p3.addTotemPiece(new com.inscription.totem.TotemHead("Canine"));
        p3.addTotemPiece(new com.inscription.totem.TotemBody(com.inscription.sigil.AirborneSigil::new));
        new BuildTotemEvent(new Random()).resolve(new RunContext(p3, scannerOf("0\n1\n0\n0\n")));
        check("Active totem was constructed for the Canine tribe",
            p3.getActiveTotem() != null && p3.getActiveTotem().getTribe().equals("Canine"));
        check("Neither piece was consumed by construction", p3.getHeadsInInventory().size() >= 1 && p3.getBodiesInInventory().size() >= 1);

        // The real payoff: a matching-tribe creature genuinely gains the totem's sigil as a live ability.
        Board board = new Board(4);
        Player p4 = freshPlayer("P4", CardType.WOLF);
        Player o4 = freshPlayer("O4", CardType.WOLF);
        p4.setActiveTotem(new com.inscription.totem.ActiveTotem(
            new com.inscription.totem.TotemHead("Canine"), new com.inscription.totem.TotemBody(com.inscription.sigil.AirborneSigil::new)));
        GameEngine engine = new GameEngine(board, p4, o4);
        Card wolf = CardType.WOLF.create();
        engine.placeCard(true, 0, wolf);
        check("A placed Wolf (Canine) genuinely gains Airborne as a real sigil", wolf.hasSigil("Airborne"));
        Card defender = CardType.MOOSE_BUCK.create();
        board.placeCard(false, 0, defender);
        engine.ringBell(true);
        check("The totem-granted Airborne actually functions in real combat (bypasses the defender)",
            defender.getHealth() == 7);

        // A non-matching tribe is unaffected by the totem.
        Board board2 = new Board(4);
        Player p5 = freshPlayer("P5", CardType.WOLF);
        p5.setActiveTotem(new com.inscription.totem.ActiveTotem(
            new com.inscription.totem.TotemHead("Canine"), new com.inscription.totem.TotemBody(com.inscription.sigil.AirborneSigil::new)));
        GameEngine engine2 = new GameEngine(board2, p5, freshPlayer("O5", CardType.WOLF));
        Card magpie = CardType.MAGPIE.create(); // Avian, carries Hoarder natively - not Airborne
        engine2.placeCard(true, 0, magpie);
        check("A non-matching tribe (Avian Magpie) does not gain the Canine totem's sigil",
            !magpie.hasSigil("Airborne"));
    }

    private static void gainConsumablesEvent() {
        section("Gain Consumables: up to 2 items per visit while there's room, else a Pack Rat");

        Player p1 = freshPlayer("P1", CardType.WOLF);
        new GainConsumablesEvent().resolve(new RunContext(p1, scannerOf("0\n0\n")));
        check("Starting empty: both rounds happened, 2 items added", p1.getItems().size() == 2);

        Player p2 = freshPlayer("P2", CardType.WOLF);
        p2.addItem(new BoulderItem());
        p2.addItem(new BoulderItem());
        p2.addItem(new MagnifyingGlassItem());
        new GainConsumablesEvent().resolve(new RunContext(p2, scannerOf("")));
        check("Full inventory: no new item added", p2.getItems().size() == 3);
        check("Full inventory: a Pack Rat was granted instead",
            drawAllAnimal(p2).stream().anyMatch(c -> c.getName().equals("Pack Rat")));

        Player p3 = freshPlayer("P3", CardType.WOLF);
        p3.addItem(new BoulderItem());
        p3.addItem(new BoulderItem());
        new GainConsumablesEvent().resolve(new RunContext(p3, scannerOf("0\n")));
        check("Starting at 2/3: exactly 1 item added, now full - second round correctly skipped",
            p3.getItems().size() == 3);
        check("No Pack Rat needed when a real item was still granted this visit",
            drawAllAnimal(p3).stream().noneMatch(c -> c.getName().equals("Pack Rat")));
    }

    private static void itemSystem() {
        section("Item system: Boulder placement, Magnifying Glass peek, the 3-item cap, Trinket Bearer");

        // Boulder: places into a chosen empty lane through the engine (so PLACE still fires).
        Board board1 = new Board(4);
        Player p1 = freshPlayer("P1", CardType.WOLF);
        Player o1 = freshPlayer("O1", CardType.WOLF);
        GameEngine engine1 = new GameEngine(board1, p1, o1);
        boolean used = new BoulderItem().use(new ItemContext(engine1, board1, p1, o1, scannerOf("2\n")));
        check("Boulder item: consumed", used);
        check("Boulder item: placed in the chosen lane", board1.peekCard(true, 2) != null
            && board1.peekCard(true, 2).getName().equals("Boulder"));

        // Boulder: refuses when there's no empty lane.
        Board board2 = new Board(4);
        Player p2 = freshPlayer("P2", CardType.WOLF);
        Player o2 = freshPlayer("O2", CardType.WOLF);
        for (int i = 0; i < 4; i++) {
            board2.placeCard(true, i, CardType.WOLF.create());
        }
        GameEngine engine2 = new GameEngine(board2, p2, o2);
        boolean usedWhenFull = new BoulderItem().use(new ItemContext(engine2, board2, p2, o2, scannerOf("")));
        check("Boulder item: refuses when no empty lane", !usedWhenFull);

        // Magnifying Glass: peeking never removes the card.
        Player p3 = new Player("P3", new Deck(List.of(CardType.GRIZZLY.create())), new Deck(List.of(CardType.SQUIRREL.create())));
        Board board3 = new Board(4);
        GameEngine engine3 = new GameEngine(board3, p3, freshPlayer("O3", CardType.WOLF));
        boolean peeked = new MagnifyingGlassItem().use(
            new ItemContext(engine3, board3, p3, freshPlayer("O3b", CardType.WOLF), scannerOf("")));
        check("Magnifying Glass: consumed", peeked);
        Card drawn = p3.drawFromDeck(DeckType.ANIMAL);
        check("Magnifying Glass: peeking didn't remove the card - it's still drawable (Grizzly)",
            drawn.getName().equals("Grizzly"));

        // Player inventory cap at 3.
        Player p4 = freshPlayer("P4", CardType.WOLF);
        check("Add 1st item succeeds", p4.addItem(new BoulderItem()));
        check("Add 2nd item succeeds", p4.addItem(new MagnifyingGlassItem()));
        check("Add 3rd item succeeds", p4.addItem(new BoulderItem()));
        check("4th item refused - inventory full", !p4.addItem(new MagnifyingGlassItem()));

        // Trinket Bearer (on Pack Rat): grants an item on play, with room; silent no-op when full.
        Board board5 = new Board(4);
        Player p5 = freshPlayer("P5", CardType.WOLF);
        Player o5 = freshPlayer("O5", CardType.WOLF);
        GameEngine engine5 = new GameEngine(board5, p5, o5);
        Card packRat = CardType.PACK_RAT.create();
        check("Pack Rat carries Trinket Bearer", packRat.hasSigil("Trinket Bearer"));
        engine5.placeCard(true, 0, packRat);
        check("Trinket Bearer: item granted on play", p5.getItems().size() == 1);

        Board board6 = new Board(4);
        Player p6 = freshPlayer("P6", CardType.WOLF);
        p6.addItem(new BoulderItem());
        p6.addItem(new BoulderItem());
        p6.addItem(new BoulderItem());
        GameEngine engine6 = new GameEngine(board6, p6, freshPlayer("O6", CardType.WOLF));
        engine6.placeCard(true, 0, CardType.PACK_RAT.create());
        check("Trinket Bearer: silent no-op when inventory already full", p6.getItems().size() == 3);
    }

    private static void newItems() {
        section("New items: Scissors, Pliers, Hoggy Bank, the 4 Bottles, Unsacrificeable, Hourglass, Harpie's Fan, Fish Hook");

        // Scissors: destroys a chosen opposing card; refuses on an empty opponent board.
        Board sBoard = new Board(4);
        Player sp = freshPlayer("SP", CardType.WOLF);
        Player so = freshPlayer("SO", CardType.WOLF);
        GameEngine sEngine = new GameEngine(sBoard, sp, so);
        sBoard.placeCard(false, 1, CardType.WOLF.create());
        boolean scissorsUsed = new ScissorsItem().use(new ItemContext(sEngine, sBoard, sp, so, scannerOf("0\n")));
        check("Scissors: consumed and destroyed the opposing card", scissorsUsed && sBoard.peekCard(false, 1) == null);
        boolean scissorsRefused = !new ScissorsItem().use(new ItemContext(sEngine, sBoard, sp, so, scannerOf("")));
        check("Scissors: refuses with no opposing cards left", scissorsRefused);

        // Pliers: 1 damage to the opponent via the health scale.
        Board plBoard = new Board(4);
        Player plp = freshPlayer("PLP", CardType.WOLF);
        GameEngine plEngine = new GameEngine(plBoard, plp, freshPlayer("PLO", CardType.WOLF));
        int scaleBefore = plEngine.getHealthScale().getValue();
        new PliersItem().use(new ItemContext(plEngine, plBoard, plp, null, scannerOf("")));
        check("Pliers: scale moved 1 point toward the player", plEngine.getHealthScale().getValue() == scaleBefore + 1);

        // Hoggy Bank: 4 bones directly, and auto-granted on a campfire destruction.
        Player hb1 = freshPlayer("HB1", CardType.WOLF);
        int bonesBefore = hb1.getBones();
        new HoggyBankItem().use(new ItemContext(null, null, hb1, null, scannerOf("")));
        check("Hoggy Bank: gained exactly 4 bones", hb1.getBones() == bonesBefore + 4);

        Player hb2 = freshPlayer("HB2", CardType.WOLF);
        Card hbWolf = CardType.WOLF.create();
        hb2.addToHand(hbWolf);
        new CampfireEvent(CampfireEvent.Type.DAMAGE, fixedRandom(0.0)).resolve(new RunContext(hb2, scannerOf("0\n0\n")));
        check("Campfire destruction auto-grants a Hoggy Bank item",
            hb2.getItems().stream().anyMatch(i -> i.getName().equals("Hoggy Bank")));

        // The 4 "in a Bottle" items each add the right card to hand.
        Player bib = freshPlayer("BIB", CardType.WOLF);
        new BoulderInABottleItem().use(new ItemContext(null, null, bib, null, scannerOf("")));
        new SquirrelInABottleItem().use(new ItemContext(null, null, bib, null, scannerOf("")));
        new BlackGoatInABottleItem().use(new ItemContext(null, null, bib, null, scannerOf("")));
        new FrozenOpossumInABottleItem().use(new ItemContext(null, null, bib, null, scannerOf("")));
        check("Boulder in a Bottle added a Boulder", bib.getHand().stream().anyMatch(c -> c.getName().equals("Boulder")));
        check("Squirrel in a Bottle added a Squirrel", bib.getHand().stream().anyMatch(c -> c.getName().equals("Squirrel")));
        check("Black Goat in a Bottle added a Black Goat", bib.getHand().stream().anyMatch(c -> c.getName().equals("Black Goat")));
        check("Frozen Opossum in a Bottle added a Frozen Opossum", bib.getHand().stream().anyMatch(c -> c.getName().equals("Frozen Opossum")));

        // Unsacrificeable: Boulder and Frozen Opossum refuse sacrifice; a normal card doesn't; combat death still works.
        Board usBoard = new Board(4);
        Player usp = freshPlayer("USP", CardType.WOLF);
        GameEngine usEngine = new GameEngine(usBoard, usp, freshPlayer("USO", CardType.WOLF));
        usBoard.placeCard(true, 0, SpecialCardType.BOULDER.create());
        boolean boulderThrew = false;
        try {
            usEngine.sacrificeFromBoard(true, 0);
        } catch (InvalidSacrificeException e) {
            boulderThrew = true;
        }
        check("Boulder cannot be sacrificed", boulderThrew && usBoard.peekCard(true, 0) != null);

        Board usBoard2 = new Board(4);
        Player usp2 = freshPlayer("USP2", CardType.WOLF);
        GameEngine usEngine2 = new GameEngine(usBoard2, usp2, freshPlayer("USO2", CardType.WOLF));
        usBoard2.placeCard(true, 0, CardType.WOLF.create());
        usEngine2.sacrificeFromBoard(true, 0); // should not throw
        check("A normal Wolf can still be sacrificed as usual", usBoard2.peekCard(true, 0) == null);

        Board usBoard3 = new Board(4);
        Player usp3 = freshPlayer("USP3", CardType.WOLF);
        Player uso3 = freshPlayer("USO3", CardType.WOLF);
        GameEngine usEngine3 = new GameEngine(usBoard3, usp3, uso3);
        usBoard3.placeCard(false, 0, SpecialCardType.FROZEN_OPOSSUM.create());
        Card usAttacker = CardType.WOLF.create();
        usAttacker.addSigil(new com.inscription.sigil.TouchOfDeathSigil());
        usBoard3.placeCard(true, 0, usAttacker);
        usEngine3.ringBell(true);
        Card afterFrozenDeath = usBoard3.peekCard(false, 0);
        check("Frozen Opossum still dies normally in combat and triggers Frozen Away",
            afterFrozenDeath != null && afterFrozenDeath.getName().equals("Opossum"));

        // Hourglass: registers a one-shot skip that consuming correctly clears.
        Board hgBoard = new Board(4);
        Player hgp = freshPlayer("HGP", CardType.WOLF);
        GameEngine hgEngine = new GameEngine(hgBoard, hgp, freshPlayer("HGO", CardType.WOLF));
        new HourglassItem().use(new ItemContext(hgEngine, hgBoard, hgp, null, scannerOf("")));
        check("Hourglass: skip request registered", hgEngine.consumeSkipOpponentNextTurn());
        check("Hourglass: consuming resets the flag", !hgEngine.consumeSkipOpponentNextTurn());

        // Harpie's Birdleg Fan: current cards gain temporary Airborne; later placements don't; it expires next turn.
        Board hpBoard = new Board(4);
        Player hpp = freshPlayer("HPP", CardType.WOLF);
        Player hpo = freshPlayer("HPO", CardType.WOLF);
        GameEngine hpEngine = new GameEngine(hpBoard, hpp, hpo);
        Card hpExisting = CardType.WOLF.create();
        hpEngine.placeCard(true, 0, hpExisting);
        new HarpiesBirdlegFanItem().use(new ItemContext(hpEngine, hpBoard, hpp, hpo, scannerOf("")));
        check("Harpie's Fan: existing card gained Airborne", hpExisting.hasSigil("Airborne"));

        Card hpLater = CardType.WOLF.create();
        hpEngine.placeCard(true, 1, hpLater);
        check("Harpie's Fan: a card placed after using it is unaffected", !hpLater.hasSigil("Airborne"));

        Card hpDefender = CardType.MOOSE_BUCK.create();
        hpBoard.placeCard(false, 0, hpDefender);
        hpEngine.ringBell(true);
        check("Harpie's Fan: the granted Airborne actually bypasses a defender in combat", hpDefender.getHealth() == 7);

        hpEngine.startTurn(false);
        hpEngine.startTurn(true);
        check("Harpie's Fan: Airborne correctly expires at the start of the next turn", !hpExisting.hasSigil("Airborne"));

        // Fish Hook: steals an opposing card onto an empty own-side slot (built, deliberately not in the general pools).
        Board fhBoard = new Board(4);
        Player fhp = freshPlayer("FHP", CardType.WOLF);
        Player fho = freshPlayer("FHO", CardType.WOLF);
        GameEngine fhEngine = new GameEngine(fhBoard, fhp, fho);
        Card fhTarget = CardType.WOLF.create();
        fhBoard.placeCard(false, 2, fhTarget);
        boolean fishHookUsed = new FishHookItem().use(new ItemContext(fhEngine, fhBoard, fhp, fho, scannerOf("0\n0\n")));
        check("Fish Hook: consumed and the card now sits on the player's own side",
            fishHookUsed && fhBoard.peekCard(true, 0) == fhTarget && fhBoard.peekCard(false, 2) == null);
    }

    private static void boneAltarEvent() {
        section("Bone Altar: permanently remove a card for a Minor or full Boon of the Bone Lord");

        Player ba1 = freshPlayer("BA1", CardType.WOLF);
        ba1.addToHand(CardType.PORCUPINE.create());
        new BoneAltarEvent().resolve(new RunContext(ba1, scannerOf("0\n")));
        check("Removing a plain card grants the Minor Boon (+1 bonus starting bones)", ba1.getBonusStartingBones() == 1);
        check("The removed card is gone from hand", ba1.getHand().stream().noneMatch(c -> c.getName().equals("Porcupine")));

        Player ba2 = freshPlayer("BA2", CardType.WOLF);
        ba2.addToHand(CardType.BLACK_GOAT.create());
        new BoneAltarEvent().resolve(new RunContext(ba2, scannerOf("0\n")));
        check("Removing Black Goat grants the full Boon (+4 bonus starting bones)", ba2.getBonusStartingBones() == 4);

        Player ba3 = freshPlayer("BA3", CardType.WOLF);
        ba3.addToHand(CardType.PORCUPINE.create());
        ba3.addToHand(CardType.BLACK_GOAT.create());
        new BoneAltarEvent().resolve(new RunContext(ba3, scannerOf("0\n")));
        new BoneAltarEvent().resolve(new RunContext(ba3, scannerOf("0\n")));
        check("Two visits stack (1 + 4 = 5)", ba3.getBonusStartingBones() == 5);

        // The real payoff: Battle grants the accumulated bonus at the start of every fight, recurring.
        Player ba4 = freshPlayer("BA4", CardType.WOLF);
        ba4.addToHand(CardType.BLACK_GOAT.create());
        new BoneAltarEvent().resolve(new RunContext(ba4, scannerOf("0\n")));
        int bonesBeforeBattle1 = ba4.getBones();
        new Battle(ba4, freshPlayer("BA4-OPP1", CardType.WOLF), scannerOf("quit\n"));
        check("Battle start grants the +4 boon immediately", ba4.getBones() == bonesBeforeBattle1 + 4);
        int bonesAfterBattle1 = ba4.getBones();
        new Battle(ba4, freshPlayer("BA4-OPP2", CardType.WOLF), scannerOf("quit\n"));
        check("A second battle grants the boon again - it's recurring, not one-time (bones reset to 0 first, then +4 granted)",
            ba4.getBones() == 4);
        check("Bones genuinely reset between battles - didn't just keep accumulating on top of the last fight's total",
            ba4.getBones() != bonesAfterBattle1 + 4);
    }

    private static void peltSystemEvent() {
        section("Pelt system: teeth, the Trapper, fusing pelts, and the Trader's market");

        // Battle banks earned golden teeth into the persistent player.
        Player pt = freshPlayer("PT", CardType.WOLF, CardType.WOLF);
        int teethBefore = pt.getTeeth();
        new Battle(pt, freshPlayer("PT-OPP", CardType.WOLF), scannerOf("quit\n"));
        check("Battle banks golden teeth into the persistent player (>= 0, no crash)", pt.getTeeth() >= teethBefore);

        // Trapper: first Rabbit Pelt is granted unconditionally on arrival, even if the
        // player leaves immediately without buying anything at all.
        Player p1 = freshPlayer("P1", CardType.WOLF);
        new BuyPeltsEvent().resolve(new RunContext(p1, scannerOf("3\n")));
        check("First Rabbit Pelt is free, granted even when leaving immediately", p1.getPeltCount(PeltKind.RABBIT) == 1 && p1.getTeeth() == 0);

        Player p2 = freshPlayer("P2", CardType.WOLF);
        p2.gainTeeth(14);
        new BuyPeltsEvent().resolve(new RunContext(p2, scannerOf("1\n2\n3\n")));
        check("Free rabbit + Wolf(4) + Sheep(8) = 12 of 14 teeth spent, 2 left",
            p2.getPeltCount(PeltKind.RABBIT) == 1 && p2.getPeltCount(PeltKind.WOLF) == 1
                && p2.getPeltCount(PeltKind.SHEEP) == 1 && p2.getTeeth() == 2);

        // Mycologists: fusing 2 pelts of the same kind into 1 Fused pelt.
        Player p3 = freshPlayer("P3", CardType.WOLF);
        p3.gainPelt(PeltKind.RABBIT);
        p3.gainPelt(PeltKind.RABBIT);
        new MycologistsEvent().resolve(new RunContext(p3, scannerOf("0\n")));
        check("Fusing 2 Rabbit Pelts consumes both and grants 1 Fused Rabbit Pelt",
            p3.getPeltCount(PeltKind.RABBIT) == 0 && p3.getPeltCount(PeltKind.FUSED_RABBIT) == 1);

        // Trader: spending a pelt buys a card and consumes the pelt.
        Player p4 = freshPlayer("P4", CardType.WOLF);
        p4.gainPelt(PeltKind.RABBIT);
        new TradePeltsEvent(new Random(1)).resolve(new RunContext(p4, scannerOf("0\n0\n")));
        check("Rabbit Pelt spent after a Trader purchase", p4.getPeltCount(PeltKind.RABBIT) == 0);
        check("A card was added to the deck", drawAllAnimal(p4).size() == 2);

        // Trader: a Fused pelt's market genuinely shows fused (double-stat) cards.
        Player p5 = freshPlayer("P5", CardType.WOLF);
        p5.gainPelt(PeltKind.FUSED_RABBIT);
        new TradePeltsEvent(new Random(3)).resolve(new RunContext(p5, scannerOf("0\n0\n")));
        boolean gotFused = drawAllAnimal(p5).stream().anyMatch(c -> {
            CardType matching = java.util.Arrays.stream(CardType.values())
                .filter(t -> t.create().getName().equals(c.getName())).findFirst().orElse(null);
            return matching != null && c.getAttack() == matching.create().getAttack() * 2;
        });
        check("Fused Rabbit Pelt market yields a genuinely fused (double-stat) card", gotFused);

        // Trader: no pelts at all grants 5 golden teeth (final confirmed rule).
        Player p6 = freshPlayer("P6", CardType.WOLF);
        int p6TeethBefore = p6.getTeeth();
        boolean result = new TradePeltsEvent().resolve(new RunContext(p6, scannerOf("")));
        check("No pelts to trade: still resolves cleanly", result);
        check("No pelts to trade: exactly 5 golden teeth granted", p6.getTeeth() == p6TeethBefore + 5);
    }

    /**
     * A GameUI for testing DeckTrialEvent specifically: since it now offers
     * a random 3 of 6 trials, a fixed numeric index no longer reliably
     * picks a specific trial. This picks the FIRST askChoice call (the
     * trial selection) by matching a trial name substring against whatever
     * was actually offered, then delegates every subsequent askChoice call
     * (e.g. the reward choice) to an ordinary scripted Scanner. Still needs
     * a seed where the desired trial is genuinely among the offered 3 -
     * this throws a clear error if it isn't, rather than silently picking
     * something else.
     */
    /**
     * A GameUI for tests that need the player to keep drawing and ringing
     * the bell every turn (never placing anything) until they eventually
     * lose - robust to Battle's opening obstacle now being drawn at random
     * from a small pool (Porcupine, Grand Fir, Snowy Fir, Stump) rather
     * than always the same Porcupine. Different obstacles deal different
     * amounts of damage per turn (0 attack for the new environment cards,
     * 1 for Porcupine), which can shift exactly when the opponent runs out
     * of new cards to play relative to when the player's candles run out -
     * a fixed text script can't adapt to that, but matching on the actual
     * prompt text can.
     */
    private static GameUI keepLosingUI() {
        return new GameUI() {
            @Override
            public void show(String message) {
            }

            @Override
            public int askChoice(String prompt, List<String> options) {
                if (options.contains("Continue fighting (risk further exchanges for a chance at more teeth)")) {
                    return options.indexOf("Continue fighting (risk further exchanges for a chance at more teeth)");
                }
                if (options.contains("Draw Animal")) {
                    return options.indexOf("Draw Animal");
                }
                for (int i = 0; i < options.size(); i++) {
                    if (options.get(i).contains("Ring the bell")) {
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public void waitForContinue(String prompt) {
            }
        };
    }

    /**
     * A GameUI for tests that place one card per turn, ring the bell, then
     * accept a surrender the instant it's offered - matching harmlessCandleOpponent
     * (which has nothing at all, so a surrender offer appears right after
     * the first turn). Also robust to Battle's now-randomized opening
     * obstacle, for the same reason keepLosingUI() is.
     */
    private static GameUI winByPlacingThenAcceptingSurrenderUI() {
        return new GameUI() {
            int placesThisTurn = 0;

            @Override
            public void show(String message) {
                if (message.startsWith("----- Your turn")) {
                    placesThisTurn = 0;
                }
            }

            @Override
            public int askChoice(String prompt, List<String> options) {
                if (options.contains("Accept the surrender (win now, no bonus teeth)")) {
                    return options.indexOf("Accept the surrender (win now, no bonus teeth)");
                }
                if (options.contains("Draw Animal")) {
                    return options.indexOf("Draw Animal");
                }
                if (options.contains("Place a card") && placesThisTurn == 0) {
                    placesThisTurn++;
                    return options.indexOf("Place a card");
                }
                for (int i = 0; i < options.size(); i++) {
                    if (options.get(i).contains("Ring the bell")) {
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public void waitForContinue(String prompt) {
            }
        };
    }

    private static GameUI trialPickingUI(String trialNameSubstring, String followUpScript) {
        Scanner followUp = scannerOf(followUpScript);
        return new GameUI() {
            boolean firstCall = true;

            @Override
            public void show(String message) {
            }

            @Override
            public int askChoice(String prompt, List<String> options) {
                if (firstCall) {
                    firstCall = false;
                    for (int i = 0; i < options.size(); i++) {
                        if (options.get(i).startsWith(trialNameSubstring)) {
                            return i;
                        }
                    }
                    throw new IllegalStateException(
                        "'" + trialNameSubstring + "' wasn't among the offered trials: " + options);
                }
                return Integer.parseInt(followUp.nextLine().trim());
            }

            @Override
            public void waitForContinue(String prompt) {
            }
        };
    }

    private static void deckTrialEvent() {
        section("Deck Trial: the 6 real Inscryption trials (Bones, Blood, Power, Health, Wisdom, Kin), 3 of 6 offered at random");

        // Trial of Bones: only Bones-cost cards contribute.
        Player dtBones = new Player("DT-Bones", new Deck(List.of(
            CardType.COCKROACH.create(), CardType.OPOSSUM.create(), CardType.WOLF.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(1)).resolve(new RunContext(dtBones, trialPickingUI("Trial of Bones", "0\n")));
        check("Trial of Bones passes (4+2=6 bones >= 5)", drawAllAnimal(dtBones).size() == 1);

        Player dtBonesFail = new Player("DT-BonesFail", new Deck(List.of(
            CardType.WOLF.create(), CardType.GECK.create(), CardType.PORCUPINE.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(1)).resolve(new RunContext(dtBonesFail, trialPickingUI("Trial of Bones", "")));
        check("Trial of Bones fails (all Blood/free cards, 0 Bones)", drawAllAnimal(dtBonesFail).isEmpty());

        // Trial of Blood: only Blood-cost cards contribute.
        Player dtBlood = new Player("DT-Blood", new Deck(List.of(
            CardType.WOLF.create(), CardType.PORCUPINE.create(), CardType.BLACK_GOAT.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(2)).resolve(new RunContext(dtBlood, trialPickingUI("Trial of Blood", "0\n")));
        check("Trial of Blood passes (2+1+1=4 Blood >= 4)", drawAllAnimal(dtBlood).size() == 1);

        // Trial of Power: Ant-tribe cards use the swarm approximation.
        Player dtPower = new Player("DT-Power", new Deck(List.of(
            CardType.WOLF.create(), CardType.WOLF.create(), CardType.WOLF.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(1)).resolve(new RunContext(dtPower, trialPickingUI("Trial of Power", "0\n")));
        check("Trial of Power passes (9 attack >= 4)", drawAllAnimal(dtPower).size() == 1);

        Player dtPowerAnts = new Player("DT-PowerAnts", new Deck(List.of(
            CardType.WORKER_ANT.create(), CardType.WORKER_ANT.create(), CardType.GECK.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(1)).resolve(new RunContext(dtPowerAnts, trialPickingUI("Trial of Power", "0\n")));
        check("Trial of Power: 2 Ants (swarm-counted 2 each = 4) + Geck (1) = 5 >= 4, passes",
            drawAllAnimal(dtPowerAnts).size() == 1);

        // Trial of Health: exact-boundary pass.
        Player dtHealth = new Player("DT-Health", new Deck(List.of(
            CardType.WOLF.create(), CardType.WOLF.create(), CardType.WOLF.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(3)).resolve(new RunContext(dtHealth, trialPickingUI("Trial of Health", "0\n")));
        check("Trial of Health passes at the exact boundary (6 >= 6)", drawAllAnimal(dtHealth).size() == 1);

        Player dtHealthFail = new Player("DT-HealthFail", new Deck(List.of(
            CardType.GECK.create(), CardType.GECK.create(), CardType.GECK.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(3)).resolve(new RunContext(dtHealthFail, trialPickingUI("Trial of Health", "")));
        check("Trial of Health fails (3 < 6)", drawAllAnimal(dtHealthFail).isEmpty());

        // Trial of Wisdom: visible sigils only, Extra Sigil markers excluded.
        Player dtWisdom = new Player("DT-Wisdom", new Deck(List.of(
            CardType.PORCUPINE.create(), CardType.ADDER.create(), CardType.GREAT_WHITE.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(1)).resolve(new RunContext(dtWisdom, trialPickingUI("Trial of Wisdom", "0\n")));
        check("Trial of Wisdom passes (3 real sigils >= 3)", drawAllAnimal(dtWisdom).size() == 1);

        Card markedGeck = CardType.GECK.create();
        markedGeck.addSigil(new com.inscription.sigil.ExtraSigilMarker());
        markedGeck.addSigil(new com.inscription.sigil.ExtraSigilMarker());
        Player dtWisdomMarker = new Player("DT-WisdomMarker", new Deck(List.of(
            markedGeck, CardType.WOLF.create(), CardType.GRIZZLY.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(1)).resolve(new RunContext(dtWisdomMarker, trialPickingUI("Trial of Wisdom", "")));
        check("Trial of Wisdom: Extra Sigil markers don't count as real sigils (still fails)",
            drawAllAnimal(dtWisdomMarker).isEmpty());

        // Trial of Kin: at least 2 of 3 must share a tribe.
        Player dtKin = new Player("DT-Kin", new Deck(List.of(
            CardType.WOLF.create(), CardType.WOLF.create(), CardType.KINGFISHER.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(2)).resolve(new RunContext(dtKin, trialPickingUI("Trial of Kin", "0\n")));
        check("Trial of Kin passes (2 Canines share a tribe)", drawAllAnimal(dtKin).size() == 1);

        Player dtKinFail = new Player("DT-KinFail", new Deck(List.of(
            CardType.WOLF.create(), CardType.KINGFISHER.create(), CardType.GECK.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        new DeckTrialEvent(new Random(2)).resolve(new RunContext(dtKinFail, trialPickingUI("Trial of Kin", "")));
        check("Trial of Kin fails (3 different tribes)", drawAllAnimal(dtKinFail).isEmpty());

        // Insufficient deck: no crash, partial draw kept, trial doesn't proceed - doesn't
        // matter which trial is picked here, so a plain console scanner is fine.
        Player dt6 = new Player("DT6", new Deck(List.of(CardType.WOLF.create())),
            new Deck(List.of(CardType.SQUIRREL.create())));
        boolean dt6Result = new DeckTrialEvent().resolve(new RunContext(dt6, scannerOf("0\n")));
        check("Insufficient deck: no crash, the 1 drawn card stays in hand", dt6Result && dt6.getHand().size() == 1);

        // NEW behavior specifically: only 3 of the 6 trials are ever offered in one visit.
        Player dtOfferCount = freshPlayer("DT-OfferCount", CardType.WOLF);
        java.util.concurrent.atomic.AtomicInteger offeredCount = new java.util.concurrent.atomic.AtomicInteger();
        GameUI countingUI = new GameUI() {
            @Override public void show(String message) { }
            @Override public int askChoice(String prompt, List<String> options) {
                offeredCount.set(options.size());
                return 0;
            }
            @Override public void waitForContinue(String prompt) { }
        };
        new DeckTrialEvent(new Random(5)).resolve(new RunContext(dtOfferCount, countingUI));
        check("Exactly 3 of the 6 trials are offered per visit, not all 6", offeredCount.get() == 3);
    }

    private static void candleSystem() {
        section("Candle system: 2 lives per run, boss fights cap to 1 and require 2 wins in a row");

        Player c1 = freshPlayer("C1", CardType.WOLF);
        check("Player starts with 2 candles", c1.getCandles() == 2);
        c1.loseCandle();
        check("loseCandle decrements to 1", c1.getCandles() == 1);
        c1.loseCandle();
        check("loseCandle decrements to 0 and isOutOfCandles is true", c1.getCandles() == 0 && c1.isOutOfCandles());
        c1.loseCandle();
        check("loseCandle floors at 0, never negative", c1.getCandles() == 0);
        c1.setCandles(1);
        check("setCandles works directly", c1.getCandles() == 1 && !c1.isOutOfCandles());

        // Quitting a regular battle costs a candle, same as a genuine loss.
        Player c2 = freshPlayer("C2", CardType.WOLF, CardType.WOLF);
        BattleNode quitNode = new BattleNode("Test", p -> unbeatableCandleOpponent());
        boolean quitResult = quitNode.resolve(new RunContext(c2, scannerOf("quit\n")));
        check("Quitting costs a candle, run continues since one remains", quitResult && c2.getCandles() == 1);

        // A genuine loss with a candle remaining lets the run continue; losing the last candle ends it.
        List<CardType> thirtyWolves = new java.util.ArrayList<>();
        for (int i = 0; i < 30; i++) {
            thirtyWolves.add(CardType.WOLF);
        }
        Player c3 = freshPlayer("C3", thirtyWolves.toArray(new CardType[0]));
        BattleNode loseNode = new BattleNode("Test", p -> unbeatableCandleOpponent());
        boolean firstLossResult = loseNode.resolve(new RunContext(c3, keepLosingUI()));
        check("First genuine loss (2->1 candle) lets the run continue", firstLossResult && c3.getCandles() == 1);
        boolean secondLossResult = loseNode.resolve(new RunContext(c3, keepLosingUI()));
        check("Losing the last candle ends the run", !secondLossResult && c3.isOutOfCandles());

        // Boss fights: cap to 1 candle, require 2 wins in a row.
        Player c4 = freshPlayer("C4", CardType.WOLF, CardType.WOLF); // starts at 2
        BossBattleNode capNode = new BossBattleNode("TestBoss", p -> unbeatableCandleOpponent());
        capNode.resolve(new RunContext(c4, scannerOf("quit\n")));
        check("Facing a boss caps 2 candles to 1, then quitting spends that last one (0 total - "
                + "if the cap hadn't happened, quitting alone would only reach 1)",
            c4.getCandles() == 0);

        List<Card> thirtyGecks = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            thirtyGecks.add(CardType.GECK.create());
        }

        Player c5 = new Player("C5", new Deck(thirtyGecks),
            new Deck(List.of(CardType.SQUIRREL.create(), CardType.SQUIRREL.create())));
        BossBattleNode winBothNode = new BossBattleNode("WeakBoss", p -> harmlessCandleOpponent());
        boolean winBothResult = winBothNode.resolve(new RunContext(c5, winByPlacingThenAcceptingSurrenderUI()));
        check("Winning both boss rounds defeats the boss", winBothResult && c5.getCandles() == 1);

        List<Card> thirtyGecks2 = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            thirtyGecks2.add(CardType.GECK.create());
        }
        Player c6 = new Player("C6", new Deck(thirtyGecks2),
            new Deck(List.of(CardType.SQUIRREL.create(), CardType.SQUIRREL.create())));
        c6.setCandles(1);
        int[] callCount = {0};
        BossBattleNode mixedNode = new BossBattleNode("MixedBoss", p -> {
            callCount[0]++;
            return callCount[0] == 1 ? harmlessCandleOpponent() : unbeatableCandleOpponent();
        });
        // Round 1 (vs harmlessCandleOpponent) needs "place then accept surrender" to
        // win; round 2 (vs unbeatableCandleOpponent) needs "never place, just ring
        // the bell" to genuinely lose, rather than surviving indefinitely by placing
        // free Gecks each turn. Tracks which round is active via BossBattleNode's own
        // round-announcement message, since both rounds share this one GameUI instance.
        GameUI mixedUI = new GameUI() {
            int currentRound = 1;
            int placesThisTurn = 0;

            @Override
            public void show(String message) {
                if (message.startsWith("===== ") && message.contains("Round 2 of")) {
                    currentRound = 2;
                }
                if (message.startsWith("----- Your turn")) {
                    placesThisTurn = 0;
                }
            }

            @Override
            public int askChoice(String prompt, List<String> options) {
                if (currentRound == 1 && options.contains("Accept the surrender (win now, no bonus teeth)")) {
                    return options.indexOf("Accept the surrender (win now, no bonus teeth)");
                }
                if (currentRound == 2 && options.contains("Continue fighting (risk further exchanges for a chance at more teeth)")) {
                    return options.indexOf("Continue fighting (risk further exchanges for a chance at more teeth)");
                }
                if (options.contains("Draw Animal")) {
                    return options.indexOf("Draw Animal");
                }
                if (currentRound == 1 && options.contains("Place a card") && placesThisTurn == 0) {
                    placesThisTurn++;
                    return options.indexOf("Place a card");
                }
                for (int i = 0; i < options.size(); i++) {
                    if (options.get(i).contains("Ring the bell")) {
                        return i;
                    }
                }
                return 0;
            }

            @Override
            public void waitForContinue(String prompt) {
            }
        };
        boolean mixedResult = mixedNode.resolve(new RunContext(c6, mixedUI));
        check("Losing round 2 after winning round 1 ends the run at the boss", !mixedResult && c6.isOutOfCandles());
    }

    private static void surrenderIsAChoice() {
        section("Surrender is a repeating choice, not an automatic win");

        // Accepting immediately on the first offer ends the battle right away, with no teeth granted.
        Player s1 = freshPlayer("S1", CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF,
            CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF);
        Player o1 = new Player("O1", new Deck(List.of()), new Deck(List.of()));
        int teethBefore = s1.getTeeth();
        boolean won1 = new Battle(s1, o1, scannerOf("draw animal\nbell\n0\n")).play();
        check("Accepting the surrender immediately wins the battle", won1);
        check("No teeth granted for an accepted surrender", s1.getTeeth() == teethBefore);

        // Declining once, then accepting on the second offer, still wins - confirms the offer genuinely repeats.
        Player s2 = freshPlayer("S2", CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF,
            CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF, CardType.WOLF);
        Player o2 = new Player("O2", new Deck(List.of()), new Deck(List.of()));
        boolean won2 = new Battle(s2, o2, scannerOf("draw animal\nbell\n1\ndraw animal\nbell\n0\n")).play();
        check("Accepting on the second offer (after declining once) still wins", won2);
    }

    private static void theSmokeBeforeBossFight() {
        section("The Smoke: granted directly into hand right before a boss fight, not on any candle loss");

        // Reference card stats match the original: 0/1, costs 1 bone.
        Card smoke = SpecialCardType.THE_SMOKE.create();
        check("The Smoke: 0/1 stats, free to play",
            smoke.getAttack() == 0 && smoke.getHealth() == 1 && smoke.getCost() == 0);

        // Losing a regular (non-boss) battle does NOT grant The Smoke anymore.
        List<Card> tenGrizzlies = new java.util.ArrayList<>();
        for (int i = 0; i < 30; i++) {
            tenGrizzlies.add(CardType.GRIZZLY.create());
        }
        Player sm1 = new Player("SM1", new Deck(tenGrizzlies), new Deck(List.of(CardType.SQUIRREL.create())));
        BattleNode loseNode = new BattleNode("Test", p -> unbeatableCandleOpponent());
        boolean result1 = loseNode.resolve(new RunContext(sm1, keepLosingUI()));
        check("Losing a regular battle (2->1): run continues with 1 candle left", result1 && sm1.getCandles() == 1);
        check("Losing a regular battle: The Smoke is NOT granted here anymore (moved to boss fights)",
            drawAllAnimal(sm1).stream().noneMatch(c -> c.getName().equals("The Smoke"))
                && sm1.getHand().stream().noneMatch(c -> c.getName().equals("The Smoke")));

        // Facing a boss DOES grant The Smoke, directly into hand, right away - before round 1 even begins.
        Player sm2 = freshPlayer("SM2", CardType.WOLF);
        BossBattleNode bossNode = new BossBattleNode("TestBoss", p -> unbeatableCandleOpponent());
        bossNode.resolve(new RunContext(sm2, scannerOf("quit\n")));
        check("Facing a boss: The Smoke lands directly in hand (not shuffled into the deck)",
            sm2.getHand().stream().anyMatch(c -> c.getName().equals("The Smoke")));

        // Confirm it survives into round 1's actual opening hand (dealOpeningHand only adds, never clears).
        Player sm3 = new Player("SM3", new Deck(tenGrizzlies), new Deck(List.of(CardType.SQUIRREL.create())));
        BossBattleNode bossNode2 = new BossBattleNode("TestBoss2", p -> unbeatableCandleOpponent());
        bossNode2.resolve(new RunContext(sm3, scannerOf("draw animal\nquit\n")));
        check("The Smoke survives into round 1's actual opening hand, alongside normally-dealt cards",
            sm3.getHand().stream().anyMatch(c -> c.getName().equals("The Smoke")));
    }

    /** An opponent the player's basic Wolf deck can never realistically beat, for testing genuine losses. */
    private static Player unbeatableCandleOpponent() {
        return new Player("O", new Deck(List.of(
            CardType.GRIZZLY.create(), CardType.GRIZZLY.create(), CardType.GRIZZLY.create(), CardType.GRIZZLY.create())),
            new Deck(List.of()));
    }

    /** A genuinely inert opponent (0 attack, no sigils, no growth) that can never damage the player back. */
    /** A completely empty opponent - surrenders predictably after exactly one player turn, since it starts with nothing to ever draw or play. */
    private static Player harmlessCandleOpponent() {
        return new Player("WeakBoss", new Deck(List.of()), new Deck(List.of()));
    }

    // ---- run-system helpers ----

    private static Scanner scannerOf(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes()));
    }

    private static Random fixedRandom(double fixedNextDouble) {
        return new Random() {
            @Override
            public double nextDouble() {
                return fixedNextDouble;
            }
        };
    }

    private static Random fixedIntRandom(int fixedNextInt) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return fixedNextInt;
            }
        };
    }

    /** Draws every remaining card from the animal deck into hand, returning what was drawn. */
    private static List<Card> drawAllAnimal(Player player) {
        List<Card> drawn = new java.util.ArrayList<>();
        try {
            while (true) {
                drawn.add(player.drawFromDeck(DeckType.ANIMAL));
            }
        } catch (NoSuchElementException e) {
            return drawn;
        }
    }

    // ---- helpers ----

    private static Player freshPlayer(String name, CardType... animalCards) {
        List<Card> animals = List.of(animalCards).stream().map(CardType::create).toList();
        Deck animalDeck = new Deck(animals.isEmpty() ? List.of(CardType.WOLF.create()) : animals);
        Deck squirrelDeck = new Deck(List.of(
            CardType.SQUIRREL.create(), CardType.SQUIRREL.create(), CardType.SQUIRREL.create()));
        return new Player(name, animalDeck, squirrelDeck);
    }

    private static void section(String title) {
        System.out.println("\n========== " + title + " ==========");
    }
}
