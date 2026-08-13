package com.inscription.model;

import com.inscription.sigil.SharpQuillsSigil;
import com.inscription.sigil.Sigil;
import com.inscription.sigil.TribeSigil;

import java.util.List;
import java.util.function.Supplier;

/**
 * The card catalog: every ordinary card in the game, defined in one place as
 * name/attack/health/cost/sigils. Most cards only differ by data, not
 * behavior, so they don't need their own class - just an entry here plus
 * whichever Sigils (0 to 3) give them an identity and/or an ability.
 * <p>
 * Cards with genuinely unique code (like BossCard overriding onAttack) still
 * get a real subclass - this catalog is for the common case, not a
 * replacement for inheritance where inheritance is actually earning its keep.
 * <p>
 * TODO - tribe cards below only carry their TribeSigil for now (stats +
 * tribe identity, no ability). The following still need their actual ability
 * sigil implemented and added to their factory list once the mechanic is
 * built and confirmed:
 * Avian: Kingfisher (dives after attacking - likely DiverSigil), Sparrow/
 *   Raven/Turkey Vulture (Flying - unblockable except by Flying), Raven Egg
 *   (transforms into Raven after 1 turn), Magpie (magnifying-glass ability,
 *   meaning unconfirmed).
 * Canine: Wolf Cub (transforms into Wolf after 1 turn), Bloodhound (unique
 *   icon, meaning unconfirmed), Alpha (looks like a buff to adjacent allies
 *   - needs an adjacency concept we don't have yet).
 * Hooved: Child 13 (infinity/dagger icon, meaning unconfirmed), Black Goat
 *   (sacrifices for 3 blood instead of 1), Elk Fawn (arrow icon +
 *   transforms after 1 turn, presumably into Elk), Elk/Pronghorn (arrow
 *   icon(s), meaning unconfirmed), Moose Buck (fist icon, meaning
 *   unconfirmed).
 * Insect: Mantis God/Mantis (fan-of-arrows icon, meaning unconfirmed), Bee
 *   (Flying), Beehive (looks like it spawns Bees over time), Worker Ant/
 *   Ant Queen (attack shown as an ant icon instead of a number - meaning
 *   unconfirmed; Ant Queen looks like it spawns Worker Ants), Cockroach
 *   (circular arrow+skull - looks like it revives), Corpse Maggots (icon
 *   meaning unconfirmed).
 * Reptile: Ouroboros (circular arrow+skull - looks like rebirth-on-death,
 *   stronger each time, matching the real card of the same name), Bullfrog
 *   (wing+shield icon, meaning unconfirmed), Skink (tail-drop/escape icon,
 *   meaning unconfirmed), Adder (skull-and-crossbones - likely poison/
 *   deathtouch), Rattler (icon unclear, needs a closer look).
 * Miscellaneous: Mole Man (wing+shield - likely Mighty Leap), Pack Rat
 *   (backpack icon - likely Trinket Bearer), Cat (infinity+dagger icon,
 *   meaning unconfirmed - possibly Many Lives, given "nine lives"), Mole
 *   (spiral/arrow icon, meaning unconfirmed), River Otter (fin/wave-tail
 *   icon, meaning unconfirmed), Skunk (stink-lines icon - likely Stinky),
 *   Warren (boxed-rabbit icon - likely Rabbit Hole), Beaver (two-cards+bug
 *   icon, meaning unconfirmed), Field Mice (boxed-mice icon, meaning
 *   unconfirmed - possibly Fecundity-like), Rat King (bone-cluster icon -
 *   likely Bone King), Great White (fin/tail icon, meaning unconfirmed),
 *   Bat (wing icon - likely Waterborne, matching the Avian wing-icon cards).
 * <p>
 * Excluded for now (too complex to implement yet): Stunted Wolf, Caged
 * Wolf, Long Elk, Strange Larva (and its further forms Strange Pupa and
 * Mothman), Stinkbug, The Daus, Stoat, Amoeba (random-sigil-on-draw), and
 * the squid-shaped cards whose names didn't render legibly.
 */
public enum CardType {

    SQUIRREL("Squirrel", 0, 1, 0, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Squirrel"))),

    PORCUPINE("Porcupine", 1, 2, 1, ResourceType.BLOOD,
        List.of(SharpQuillsSigil::new)),

    // ----- Avian -----

    KINGFISHER("Kingfisher", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"))),

    RAVEN_EGG("Raven Egg", 0, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"))),

    SPARROW("Sparrow", 1, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"))),

    MAGPIE("Magpie", 1, 1, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"))),

    RAVEN("Raven", 2, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"))),

    TURKEY_VULTURE("Turkey Vulture", 3, 3, 8, ResourceType.BONES,
        List.of(() -> new TribeSigil("Avian"))),

    // ----- Canine -----

    WOLF_CUB("Wolf Cub", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Canine"))),

    BLOODHOUND("Bloodhound", 2, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Canine"))),

    WOLF("Wolf", 3, 2, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Canine"))),

    COYOTE("Coyote", 2, 1, 4, ResourceType.BONES,
        List.of(() -> new TribeSigil("Canine"))),

    ALPHA("Alpha", 1, 2, 4, ResourceType.BONES,
        List.of(() -> new TribeSigil("Canine"))),

    // ----- Hooved -----

    CHILD_13("Child 13", 0, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"))),

    BLACK_GOAT("Black Goat", 0, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"))),

    ELK_FAWN("Elk Fawn", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"))),

    ELK("Elk", 2, 4, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"))),

    PRONGHORN("Pronghorn", 1, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"))),

    MOOSE_BUCK("Moose Buck", 3, 7, 3, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"))),

    // ----- Insect -----

    MANTIS_GOD("Mantis God", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    BEE("Bee", 1, 1, 0, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    BEEHIVE("Beehive", 0, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    MANTIS("Mantis", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    RING_WORM("Ring Worm", 0, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    WORKER_ANT("Worker Ant", 0, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    ANT_QUEEN("Ant Queen", 0, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    COCKROACH("Cockroach", 1, 1, 4, ResourceType.BONES,
        List.of(() -> new TribeSigil("Insect"))),

    CORPSE_MAGGOTS("Corpse Maggots", 1, 2, 5, ResourceType.BONES,
        List.of(() -> new TribeSigil("Insect"))),

    // ----- Reptile -----

    GECK("Geck", 1, 1, 0, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    OUROBOROS("Ouroboros", 1, 1, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    BULLFROG("Bullfrog", 1, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    SKINK("Skink", 1, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    ADDER("Adder", 1, 1, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    RIVER_SNAPPER("River Snapper", 1, 6, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    RATTLER("Rattler", 3, 1, 6, ResourceType.BONES,
        List.of(() -> new TribeSigil("Reptile"))),

    // ----- Miscellaneous (no tribe) -----

    MOLE_MAN("Mole Man", 0, 6, 1, ResourceType.BLOOD, List.of()),

    AMALGAM("Amalgam", 3, 3, 2, ResourceType.BLOOD, List.of()),

    PACK_RAT("Pack Rat", 2, 2, 2, ResourceType.BLOOD, List.of()),

    URAYULI("Urayuli", 7, 7, 4, ResourceType.BLOOD, List.of()),

    CAT("Cat", 0, 1, 1, ResourceType.BLOOD, List.of()),

    MOLE("Mole", 0, 4, 1, ResourceType.BLOOD, List.of()),

    RIVER_OTTER("River Otter", 1, 1, 1, ResourceType.BLOOD, List.of()),

    SKUNK("Skunk", 0, 3, 1, ResourceType.BLOOD, List.of()),

    WARREN("Warren", 0, 2, 1, ResourceType.BLOOD, List.of()),

    BEAVER("Beaver", 1, 3, 1, ResourceType.BLOOD, List.of()),

    FIELD_MICE("Field Mice", 2, 2, 2, ResourceType.BLOOD, List.of()),

    RAT_KING("Rat King", 2, 1, 2, ResourceType.BLOOD, List.of()),

    GREAT_WHITE("Great White", 4, 2, 3, ResourceType.BLOOD, List.of()),

    GRIZZLY("Grizzly", 4, 6, 3, ResourceType.BLOOD, List.of()),

    OPOSSUM("Opossum", 1, 1, 2, ResourceType.BONES, List.of()),

    BAT("Bat", 2, 1, 4, ResourceType.BONES, List.of());

    // Add new ordinary cards as one line above - name, attack, health, cost,
    // costType, then a list of Sigil suppliers (identity tags and/or
    // abilities). Leave the list empty for a plain vanilla card.

    private final String cardName;
    private final int attack;
    private final int health;
    private final int cost;
    private final ResourceType costType;
    private final List<Supplier<Sigil>> sigilFactories;

    CardType(String cardName, int attack, int health, int cost, ResourceType costType,
             List<Supplier<Sigil>> sigilFactories) {
        this.cardName = cardName;
        this.attack = attack;
        this.health = health;
        this.cost = cost;
        this.costType = costType;
        this.sigilFactories = sigilFactories;
    }

    /**
     * Builds a fresh Card instance for this catalog entry. Each call gets its
     * own independent Sigil objects (important - sigils like Diver carry
     * per-card state, so two of the same card must not share one sigil
     * instance).
     */
    public Card create() {
        Card card = new Card(cardName, attack, health, cost, costType);
        for (Supplier<Sigil> factory : sigilFactories) {
            card.addSigil(factory.get());
        }
        return card;
    }
}
