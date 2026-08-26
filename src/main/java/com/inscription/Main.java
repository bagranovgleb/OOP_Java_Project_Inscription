package com.inscription;

import com.inscription.board.Board;
import com.inscription.deck.Deck;
import com.inscription.engine.GameEngine;
import com.inscription.exception.ConflictingModifierException;
import com.inscription.exception.InsufficientResourcesException;
import com.inscription.exception.SlotOccupiedException;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.DeckType;
import com.inscription.model.ResourceType;
import com.inscription.modifier.LimitedSacrificesModifier;
import com.inscription.modifier.RestrictedDrawModifier;
import com.inscription.modifier.SigilTotemModifier;
import com.inscription.player.Player;
import com.inscription.sigil.AirborneSigil;
import com.inscription.sigil.BifurcatedStrikeSigil;
import com.inscription.sigil.BoneKingSigil;
import com.inscription.sigil.BurrowerSigil;
import com.inscription.sigil.DamBuilderSigil;
import com.inscription.sigil.DiverSigil;
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

import java.util.List;

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
        diver();
        squirrelTotem();
        fledgling();
        sprinter();
        hefty();
        damBuilder();
        leader();
        guardian();
        looseTail();
        conflictingChallengeModifiers();
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
        section("Costs and resources: sacrifice for blood, spend it, catch overspend");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF, CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card squirrel = CardType.SQUIRREL.create();
        board.placeCard(true, 0, squirrel);
        engine.sacrificeFromBoard(true, 0);
        System.out.println("Sacrificed a squirrel -> blood: " + player.getBlood()
            + ", bones: " + player.getBones());

        player.spendResource(ResourceType.BLOOD, 1);
        System.out.println("Spent 1 blood -> remaining: " + player.getBlood());
        try {
            player.spendResource(ResourceType.BLOOD, 5);
        } catch (InsufficientResourcesException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
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

        Card worthy = CardType.SQUIRREL.create();
        worthy.addSigil(new WorthySacrificeSigil());
        board.placeCard(true, 1, worthy);
        int bloodBefore = player.getBlood();
        engine.sacrificeFromBoard(true, 1);
        System.out.println("Worthy Sacrifice granted " + (player.getBlood() - bloodBefore) + " blood");
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

    private static void diver() {
        section("Diver: submerges (untargetable) right after attacking, resurfaces at turn start");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card diverCard = CardType.SQUIRREL.create();
        diverCard.addSigil(new DiverSigil());
        board.placeCard(false, 0, diverCard);
        System.out.println("Can be targeted before it ever attacks? " + diverCard.canBeTargeted());

        engine.startTurn(false);
        engine.ringBell(false); // it attacks, then submerges
        System.out.println("Can be targeted right after attacking?  " + diverCard.canBeTargeted());

        board.placeCard(true, 0, CardType.WOLF.create());
        int scaleBefore = engine.getHealthScale().getValue();
        engine.ringBell(true); // attacking its lane should redirect to face while submerged
        System.out.println("Submerged and untouched: " + diverCard
            + " | attack redirected to face (scale changed): " + (engine.getHealthScale().getValue() != scaleBefore));

        engine.startTurn(false);
        System.out.println("Can be targeted after its turn starts (resurfaced)? " + diverCard.canBeTargeted());
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
        section("Hefty: at end of turn, pushes itself AND both neighbors one lane right");
        Board board = new Board(4);
        Player player = freshPlayer("Player", CardType.WOLF);
        Player opponent = freshPlayer("Opponent", CardType.WOLF);
        GameEngine engine = new GameEngine(board, player, opponent);

        Card left = CardType.WOLF.create();
        Card mooseBuck = CardType.MOOSE_BUCK.create();
        board.placeCard(true, 0, left);
        board.placeCard(true, 1, mooseBuck);
        engine.ringBell(true);
        System.out.println("After bell: lane 1 (was left neighbor) = " + board.peekCard(true, 1)
            + ", lane 2 (Moose Buck itself) = " + board.peekCard(true, 2));
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
