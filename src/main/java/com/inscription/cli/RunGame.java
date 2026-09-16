package com.inscription.cli;

import com.inscription.deck.Deck;
import com.inscription.deck.StarterDecks;
import com.inscription.model.CardType;
import com.inscription.player.Player;
import com.inscription.run.BattleNode;
import com.inscription.run.BossBattleNode;
import com.inscription.run.NodeContent;
import com.inscription.run.PathNode;
import com.inscription.run.PathRunner;
import com.inscription.run.event.BoneAltarEvent;
import com.inscription.run.event.BuildTotemEvent;
import com.inscription.run.event.BuyPeltsEvent;
import com.inscription.run.event.CampfireEvent;
import com.inscription.run.event.CardChoiceEvent;
import com.inscription.run.event.DeckTrialEvent;
import com.inscription.run.event.GainConsumablesEvent;
import com.inscription.run.event.MycologistsEvent;
import com.inscription.run.event.MysteriousStonesEvent;
import com.inscription.run.event.ProspectorEvent;
import com.inscription.run.event.TradePeltsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * Playable entry point for a run: a start menu, then all 3 locations
 * chained together (Woodlands -> Location 2 -> Location 3), leading up to
 * a final Leshy fight (Leshy himself isn't built yet - the run currently
 * ends after Location 3's boss). Locations 2 and 3 are placeholder copies
 * of Woodlands' shape and content for now (see buildLocation2Path() /
 * buildLocation3Path() and their deck fields) - meant to be edited into
 * their own distinct themes independently of Woodlands and each other.
 * See buildLocationPath() for the shared shape all 3 locations use.
 */
public class RunGame {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        if (!showStartMenu(scanner)) {
            scanner.close();
            return;
        }

        Player player = new Player("You", StarterDecks.beginnerAnimalDeck(), StarterDecks.beginnerSquirrelDeck());
        player.gainPelt(com.inscription.pelt.PeltKind.RABBIT);
        player.gainPelt(com.inscription.pelt.PeltKind.RABBIT);

        System.out.println("=== Inscription: a run begins ===");
        PathRunner runner = new PathRunner(player, scanner);
        if (!runner.run(buildWoodlandsPath())) {
            scanner.close();
            return;
        }
        System.out.println("\n=== Woodlands complete! Entering Location 2 ===");
        if (!runner.run(buildLocation2Path())) {
            scanner.close();
            return;
        }
        System.out.println("\n=== Location 2 complete! Entering Location 3 ===");
        runner.run(buildLocation3Path());
        scanner.close();
    }

    /** Shows a simple start menu. Returns true if the player chose to start, false if they chose to stop. */
    private static boolean showStartMenu(Scanner scanner) {
        System.out.println("==============================");
        System.out.println("          INSCRIPTION");
        System.out.println("==============================");
        while (true) {
            System.out.println("[1] Start");
            System.out.println("[2] Stop");
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            switch (line) {
                case "1" -> {
                    return true;
                }
                case "2" -> {
                    System.out.println("Goodbye!");
                    return false;
                }
                default -> System.out.println("Enter 1 or 2.");
            }
        }
    }

    /** A named enemy deck - a themed pool of CardTypes, played with repetition (see the 5 decks below). */
    private record EnemyDeck(String name, List<CardType> cardTypes) {
    }

    /** Both ends of a built location's path - needed to connect one location's boss fight to the next location's opening. */
    private record LocationPath(PathNode opening, PathNode bossFight) {
    }

    private static final EnemyDeck WOODLANDS_AVIAN_SCOUTS = new EnemyDeck("Avian Scouts", List.of(
        CardType.SPARROW, CardType.SPARROW,
        CardType.KINGFISHER, CardType.KINGFISHER,
        CardType.COYOTE,
        CardType.RAVEN_EGG, CardType.RAVEN_EGG,
        CardType.RAVEN));

    private static final EnemyDeck WOODLANDS_CANINE_PACK = new EnemyDeck("Canine Pack", List.of(
        CardType.WOLF_CUB, CardType.WOLF_CUB, CardType.WOLF_CUB,
        CardType.WOLF, CardType.WOLF, CardType.WOLF,
        CardType.COYOTE, CardType.COYOTE));

    private static final EnemyDeck WOODLANDS_HOOVED_HERD = new EnemyDeck("Hooved Herd", List.of(
        CardType.ELK_FAWN, CardType.ELK_FAWN, CardType.ELK_FAWN,
        CardType.ELK, CardType.ELK,
        CardType.PRONGHORN, CardType.PRONGHORN, CardType.PRONGHORN));

    private static final EnemyDeck WOODLANDS_AMBUSH = new EnemyDeck("Woodland Ambush", List.of(
        CardType.RAVEN, CardType.RAVEN,
        CardType.BLOODHOUND, CardType.BLOODHOUND,
        CardType.MOOSE_BUCK, CardType.MOOSE_BUCK,
        CardType.TURKEY_VULTURE, CardType.TURKEY_VULTURE));

    /** The Woodlands boss - a real Inscryption boss name, distinct from (and unrelated to) the ProspectorEvent map event of the same name. */
    private static final EnemyDeck WOODLANDS_BOSS = new EnemyDeck("The Prospector", List.of(
        CardType.ALPHA, CardType.ALPHA,
        CardType.MOOSE_BUCK, CardType.MOOSE_BUCK,
        CardType.TURKEY_VULTURE, CardType.TURKEY_VULTURE,
        CardType.RAVEN, CardType.WOLF,
        CardType.BLOODHOUND));

    /**
     * Location 2 - the Wetlands, themed around the Avian, Insect, and
     * Reptile tribes, ending in a fight against The Angler.
     */
    private static final EnemyDeck WETLANDS_AVIAN_SCOUTS = new EnemyDeck("Avian Scouts", List.of(
        CardType.BEE, CardType.BEE,
        CardType.SPARROW,
        CardType.KINGFISHER,
        CardType.RIVER_SNAPPER,
        CardType.RAVEN_EGG, CardType.RAVEN_EGG,
        CardType.RAVEN));

    private static final EnemyDeck WETLANDS_REPTILES = new EnemyDeck("Reptiles", List.of(
        CardType.BULLFROG, CardType.BULLFROG,
        CardType.SKINK, CardType.SKINK,
        CardType.GECK, CardType.GECK,
        CardType.RIVER_SNAPPER,
        CardType.ADDER));

    private static final EnemyDeck WETLANDS_INSECT_STORM = new EnemyDeck("Insect Storm", List.of(
        CardType.WORKER_ANT, CardType.WORKER_ANT, CardType.WORKER_ANT, CardType.WORKER_ANT,
        CardType.BEE, CardType.BEE,
        CardType.COCKROACH,
        CardType.MANTIS, CardType.MANTIS,
        CardType.ANT_QUEEN,
        CardType.BEEHIVE));

    /** Originally given as 9 cards against a stated 10 (flagged at the time); now exactly 10 with Rat King added. */
    private static final EnemyDeck WETLANDS_SWAMP = new EnemyDeck("Swamp", List.of(
        CardType.WORKER_ANT, CardType.WORKER_ANT,
        CardType.BEE,
        CardType.ADDER,
        CardType.MANTIS_GOD,
        CardType.RIVER_SNAPPER,
        CardType.RATTLER,
        CardType.RAVEN_EGG, CardType.RAVEN_EGG,
        CardType.RAT_KING));

    private static final EnemyDeck WETLANDS_BOSS = new EnemyDeck("The Angler", List.of(
        CardType.KINGFISHER, CardType.KINGFISHER,
        CardType.RAVEN_EGG, CardType.RAVEN_EGG,
        CardType.RAVEN,
        CardType.MANTIS, CardType.MANTIS,
        CardType.RATTLER,
        CardType.MANTIS_GOD,
        CardType.RIVER_SNAPPER,
        CardType.ADDER, CardType.ADDER));

    /**
     * Location 3 - the Snowlands, themed around the Avian and Hooved
     * tribes, ending in a fight against The Butcher. Named "The Butcher"
     * rather than "The Trapper" (the other name given alongside it) since
     * "The Trapper" already refers to a different existing character (the
     * pelt-economy NPC in BuyPeltsEvent) - reusing it here would create
     * real, ongoing confusion between two different things with the same
     * name, the same reasoning as why the Woodlands boss is "The
     * Prospector" and not reusing ProspectorEvent's name.
     */
    private static final EnemyDeck SNOWLANDS_AVIAN_SCOUTS = new EnemyDeck("Avian Scouts", List.of(
        CardType.RAVEN_EGG, CardType.RAVEN_EGG,
        CardType.SPARROW,
        CardType.WOLF_CUB,
        CardType.WOLF,
        CardType.ELK_FAWN, CardType.ELK_FAWN,
        CardType.RAVEN));

    /** As given, this is 9 cards, not the stated 8 - flagged, not silently changed. */
    private static final EnemyDeck SNOWLANDS_FLYING_MURDER = new EnemyDeck("Flying Murder", List.of(
        CardType.RAVEN, CardType.RAVEN, CardType.RAVEN,
        CardType.SPARROW,
        CardType.TURKEY_VULTURE,
        CardType.WOLF_CUB,
        CardType.RAVEN_EGG, CardType.RAVEN_EGG,
        CardType.ELK));

    private static final EnemyDeck SNOWLANDS_HOOVED_MIGRATION = new EnemyDeck("Hooved Migration", List.of(
        CardType.ELK_FAWN, CardType.ELK_FAWN,
        CardType.ELK, CardType.ELK,
        CardType.PRONGHORN,
        CardType.MOOSE_BUCK,
        CardType.WOLF_CUB,
        CardType.SPARROW, CardType.SPARROW));

    private static final EnemyDeck SNOWLANDS_WOLF_PACK = new EnemyDeck("Wolf Pack", List.of(
        CardType.WOLF_CUB, CardType.WOLF_CUB,
        CardType.WOLF, CardType.WOLF,
        CardType.BLOODHOUND,
        CardType.ALPHA,
        CardType.COYOTE, CardType.COYOTE));

    private static final EnemyDeck SNOWLANDS_BOSS = new EnemyDeck("The Butcher", List.of(
        CardType.SPARROW,
        CardType.RAVEN,
        CardType.WOLF_CUB, CardType.WOLF_CUB, CardType.WOLF_CUB,
        CardType.WOLF,
        CardType.MOOSE_BUCK,
        CardType.ALPHA,
        CardType.COYOTE, CardType.COYOTE,
        CardType.ELK_FAWN, CardType.ELK_FAWN,
        CardType.PRONGHORN, CardType.PRONGHORN));

    /**
     * Builds one location's path: Trader (opening) -> Fight -> 3 events ->
     * Fight -> 4 events -> Fight -> 4 events -> Boss Fight. The 3 regular
     * fights are drawn from the 4 given themed enemy decks - shuffled and 3
     * of the 4 picked fresh each playthrough, so which 3 you face (and in
     * what order) varies. The event pool has exactly 11 kinds, and the 3
     * stretches between fights need exactly 3 + 4 + 4 = 11 slots, so every
     * kind appears exactly once per location with no duplicates (a
     * byproduct of shuffling all 11 once and handing them out in order, not
     * something specially enforced).
     * <p>
     * Shared by all 3 locations - each supplies its own deck pools and
     * boss, but the shape itself only needs to be written once.
     */
    private static LocationPath buildLocationPath(List<EnemyDeck> deckPools, EnemyDeck boss, boolean isSnowLevel) {
        List<EnemyDeck> regularPools = new ArrayList<>(deckPools);
        Collections.shuffle(regularPools);
        List<EnemyDeck> chosen = regularPools.subList(0, 3);

        List<Supplier<NodeContent>> remainingKinds = new ArrayList<>(List.of(
            () -> new CardChoiceEvent(randomCardChoiceMode()),
            MycologistsEvent::new,
            MysteriousStonesEvent::new,
            () -> new CampfireEvent(randomCampfireType()),
            ProspectorEvent::new,
            BuildTotemEvent::new,
            GainConsumablesEvent::new,
            BoneAltarEvent::new,
            BuyPeltsEvent::new,
            TradePeltsEvent::new,
            DeckTrialEvent::new));
        Collections.shuffle(remainingKinds);

        PathNode opening = new PathNode(new TradePeltsEvent());
        PathNode fight1 = new PathNode(new BattleNode(chosen.get(0).name(), p -> enemyFromDeck(chosen.get(0), p), isSnowLevel));
        PathNode event1a = new PathNode(remainingKinds.get(0).get());
        PathNode event1b = new PathNode(remainingKinds.get(1).get());
        PathNode event1c = new PathNode(remainingKinds.get(2).get());
        PathNode fight2 = new PathNode(new BattleNode(chosen.get(1).name(), p -> enemyFromDeck(chosen.get(1), p), isSnowLevel));
        PathNode event2a = new PathNode(remainingKinds.get(3).get());
        PathNode event2b = new PathNode(remainingKinds.get(4).get());
        PathNode event2c = new PathNode(remainingKinds.get(5).get());
        PathNode event2d = new PathNode(remainingKinds.get(6).get());
        PathNode fight3 = new PathNode(new BattleNode(chosen.get(2).name(), p -> enemyFromDeck(chosen.get(2), p), isSnowLevel));
        PathNode event3a = new PathNode(remainingKinds.get(7).get());
        PathNode event3b = new PathNode(remainingKinds.get(8).get());
        PathNode event3c = new PathNode(remainingKinds.get(9).get());
        PathNode event3d = new PathNode(remainingKinds.get(10).get());
        PathNode bossFight = new PathNode(new BossBattleNode(boss.name(), p -> enemyFromDeck(boss, p), isSnowLevel));

        opening.connectTo(fight1);
        fight1.connectTo(event1a);
        event1a.connectTo(event1b);
        event1b.connectTo(event1c);
        event1c.connectTo(fight2);
        fight2.connectTo(event2a);
        event2a.connectTo(event2b);
        event2b.connectTo(event2c);
        event2c.connectTo(event2d);
        event2d.connectTo(fight3);
        fight3.connectTo(event3a);
        event3a.connectTo(event3b);
        event3b.connectTo(event3c);
        event3c.connectTo(event3d);
        event3d.connectTo(bossFight);

        return new LocationPath(opening, bossFight);
    }

    /** The Woodlands: the first location, themed around the Avian, Canine, and Hooved tribes. Public so a future GUI entry point can reuse this without duplicating deck content. */
    public static PathNode buildWoodlandsPath() {
        return buildLocationPath(
            List.of(WOODLANDS_AVIAN_SCOUTS, WOODLANDS_CANINE_PACK, WOODLANDS_HOOVED_HERD, WOODLANDS_AMBUSH),
            WOODLANDS_BOSS, false).opening();
    }

    /** Location 2: the Wetlands. Public so a future GUI entry point can reuse this without duplicating deck content. */
    public static PathNode buildLocation2Path() {
        return buildLocationPath(
            List.of(WETLANDS_AVIAN_SCOUTS, WETLANDS_REPTILES, WETLANDS_INSECT_STORM, WETLANDS_SWAMP),
            WETLANDS_BOSS, false).opening();
    }

    /** Location 3: the Snowlands - a Snowline level, so Grand Fir obstacles become Snowy Fir. Public so a future GUI entry point can reuse this without duplicating deck content. */
    public static PathNode buildLocation3Path() {
        return buildLocationPath(
            List.of(SNOWLANDS_AVIAN_SCOUTS, SNOWLANDS_FLYING_MURDER, SNOWLANDS_HOOVED_MIGRATION, SNOWLANDS_WOLF_PACK),
            SNOWLANDS_BOSS, true).opening();
    }

    /**
    /**
     * Builds an opponent whose animal deck is sized to match the player's
     * CURRENT animal deck size, drawn from the same themed pool as before
     * (with repeats, cycling through a freshly shuffled copy of the pool as
     * many times as needed to reach the target count). This keeps card-count
     * parity between player and opponent throughout a run: without it, a
     * player who started with a small deck (or lost cards along the way)
     * could face opponents with a structurally larger card pool to draw
     * from, letting the AI simply outlast them regardless of skill.
     */
    private static Player enemyFromDeck(EnemyDeck deck, Player currentPlayer) {
        int targetSize = currentPlayer.getAnimalDeckSize();
        List<CardType> pool = deck.cardTypes();
        Random random = new Random();
        List<CardType> enemyCardTypes = new ArrayList<>();
        while (enemyCardTypes.size() < targetSize) {
            List<CardType> shuffledPool = new ArrayList<>(pool);
            Collections.shuffle(shuffledPool, random);
            for (CardType type : shuffledPool) {
                if (enemyCardTypes.size() >= targetSize) {
                    break;
                }
                enemyCardTypes.add(type);
            }
        }
        return new Player(deck.name(),
            new Deck(enemyCardTypes.stream().map(CardType::create).toList()),
            new Deck(List.of()));
    }

    private static CardChoiceEvent.Mode randomCardChoiceMode() {
        CardChoiceEvent.Mode[] modes = CardChoiceEvent.Mode.values();
        return modes[new Random().nextInt(modes.length)];
    }

    private static CampfireEvent.Type randomCampfireType() {
        CampfireEvent.Type[] types = CampfireEvent.Type.values();
        return types[new Random().nextInt(types.length)];
    }
}
