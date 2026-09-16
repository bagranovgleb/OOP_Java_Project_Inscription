package com.inscription.model;

import com.inscription.sigil.AirborneSigil;
import com.inscription.sigil.AntSpawnerSigil;
import com.inscription.sigil.BeesWithinSigil;
import com.inscription.sigil.BifurcatedStrikeSigil;
import com.inscription.sigil.BoneKingSigil;
import com.inscription.sigil.BurrowerSigil;
import com.inscription.sigil.CorpseEaterSigil;
import com.inscription.sigil.DamBuilderSigil;
import com.inscription.sigil.FecunditySigil;
import com.inscription.sigil.FledglingSigil;
import com.inscription.sigil.GuardianSigil;
import com.inscription.sigil.HeftySigil;
import com.inscription.sigil.HoarderSigil;
import com.inscription.sigil.LeaderSigil;
import com.inscription.sigil.LooseTailSigil;
import com.inscription.sigil.ManyLivesSigil;
import com.inscription.sigil.MightyLeapSigil;
import com.inscription.sigil.RabbitHoleSigil;
import com.inscription.sigil.SharpQuillsSigil;
import com.inscription.sigil.Sigil;
import com.inscription.sigil.SprinterSigil;
import com.inscription.sigil.StinkySigil;
import com.inscription.sigil.TouchOfDeathSigil;
import com.inscription.sigil.TrinketBearerSigil;
import com.inscription.sigil.TribeSigil;
import com.inscription.sigil.TrifurcatedStrikeSigil;
import com.inscription.sigil.UnkillableSigil;
import com.inscription.sigil.WaterborneSigil;
import com.inscription.sigil.WorthySacrificeSigil;

import java.util.List;
import java.util.function.Supplier;

/**
 * The card catalog: every ordinary card in the game, defined in one place as
 * name/attack/health/cost/sigils. Most cards only differ by data, not
 * behavior, so they don't need their own class - just an entry here plus
 * whichever Sigils (0 to 3) give them an identity and/or an ability.
 * <p>
 * Cards with genuinely unique code (behavior beyond stats + sigils) would
 * still get a real subclass - this catalog is for the common case, not a
 * replacement for inheritance where inheritance is actually earning its keep.
 * <p>
 * Icon-to-sigil matches below were confirmed by direct visual comparison
 * against the sigils reference sheet, not guessed - in particular this
 * corrected an earlier tentative guess: the plain wing-curl icon (Sparrow,
 * Raven, Turkey Vulture, Bee, Bat) is Airborne, not Waterborne - Waterborne's
 * actual icon is a fish-tail-over-waves shape, matching Great White instead.
 * <p>
 * Assumption flagged for review: Hefty (Moose Buck) and Sprinter (Elk) both
 * need a movement direction and neither card's art made the intended arrow
 * direction unambiguous - defaulted both to RIGHT. Easy to flip later if
 * that's wrong.
 * <p>
 * TODO - still unresolved (icon meaning unclear, or the matching sigil isn't
 * built yet):
 * Hooved: Pronghorn (crescent+arrow, likely Sprinter-related - genuinely
 *   unclear whether it's a second Sprinter or something else).
 * Reptile: Rattler (icon unclear, needs a closer look).
 * Miscellaneous: River Otter
 *   (fin/wave-tail icon, meaning unconfirmed - close to Great White's
 *   Waterborne icon but not a confident enough match to commit to), Field
 *   Mice (boxed-mice icon, meaning unconfirmed - visually different enough
 *   from Fecundity's icon that it wasn't a confident match either).
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

    // ----- Environment / obstacle cards -----
    // 0 attack, no sigils - map terrain rather than a real creature. Never
    // drawn or placed from a deck by either side; only ever pre-placed
    // directly onto the board (see Battle's constructor). Costs 0 Blood
    // purely because CardType requires some cost - it's never actually
    // paid, since these are never placed through the normal hand/cost flow.
    GRAND_FIR("Grand Fir", 0, 3, 0, ResourceType.BLOOD, List.of(MightyLeapSigil::new)),
    SNOWY_FIR("Snowy Fir", 0, 4, 0, ResourceType.BLOOD, List.of(MightyLeapSigil::new)),
    STUMP("Stump", 0, 3, 0, ResourceType.BLOOD, List.of()),

    // ----- Avian -----

    KINGFISHER("Kingfisher", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"), WaterborneSigil::new, AirborneSigil::new)),

    RAVEN("Raven", 2, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"), AirborneSigil::new)),

    RAVEN_EGG("Raven Egg", 0, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"), () -> new FledglingSigil(CardType.RAVEN::create))),

    SPARROW("Sparrow", 1, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"), AirborneSigil::new)),

    MAGPIE("Magpie", 1, 1, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Avian"), HoarderSigil::new)),

    TURKEY_VULTURE("Turkey Vulture", 3, 3, 8, ResourceType.BONES,
        List.of(() -> new TribeSigil("Avian"), AirborneSigil::new)),

    // ----- Canine -----

    WOLF("Wolf", 3, 2, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Canine"))),

    WOLF_CUB("Wolf Cub", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Canine"), () -> new FledglingSigil(CardType.WOLF::create))),

    BLOODHOUND("Bloodhound", 2, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Canine"), GuardianSigil::new)),

    COYOTE("Coyote", 2, 1, 4, ResourceType.BONES,
        List.of(() -> new TribeSigil("Canine"))),

    ALPHA("Alpha", 1, 2, 4, ResourceType.BONES,
        List.of(() -> new TribeSigil("Canine"), LeaderSigil::new)),

    // ----- Hooved -----

    ELK("Elk", 2, 4, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"), () -> new SprinterSigil(SprinterSigil.RIGHT))),

    CHILD_13("Child 13", 0, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"), ManyLivesSigil::new)),

    BLACK_GOAT("Black Goat", 0, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"), WorthySacrificeSigil::new)),

    ELK_FAWN("Elk Fawn", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"), () -> new FledglingSigil(CardType.ELK::create))),

    PRONGHORN("Pronghorn", 1, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"), () -> new SprinterSigil(SprinterSigil.RIGHT))),

    MOOSE_BUCK("Moose Buck", 3, 7, 3, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Hooved"), () -> new HeftySigil(HeftySigil.RIGHT))),

    // ----- Insect -----

    MANTIS_GOD("Mantis God", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"), TrifurcatedStrikeSigil::new)),

    BEE("Bee", 1, 1, 0, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"), AirborneSigil::new)),

    BEEHIVE("Beehive", 0, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"), BeesWithinSigil::new)),

    MANTIS("Mantis", 1, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"), BifurcatedStrikeSigil::new)),

    RING_WORM("Ring Worm", 0, 1, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"))),

    WORKER_ANT("Worker Ant", 0, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"), () -> new TribeSigil("Ant"))),

    ANT_QUEEN("Ant Queen", 0, 3, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Insect"), () -> new TribeSigil("Ant"), AntSpawnerSigil::new)),

    COCKROACH("Cockroach", 1, 1, 4, ResourceType.BONES,
        List.of(() -> new TribeSigil("Insect"), UnkillableSigil::new)),

    CORPSE_MAGGOTS("Corpse Maggots", 1, 2, 5, ResourceType.BONES,
        List.of(() -> new TribeSigil("Insect"), CorpseEaterSigil::new)),

    // ----- Reptile -----

    GECK("Geck", 1, 1, 0, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    OUROBOROS("Ouroboros", 1, 1, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"), UnkillableSigil::new)),

    BULLFROG("Bullfrog", 1, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"), MightyLeapSigil::new)),

    SKINK("Skink", 1, 2, 1, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"), LooseTailSigil::new)),

    ADDER("Adder", 1, 1, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"), TouchOfDeathSigil::new)),

    RIVER_SNAPPER("River Snapper", 1, 6, 2, ResourceType.BLOOD,
        List.of(() -> new TribeSigil("Reptile"))),

    RATTLER("Rattler", 3, 1, 6, ResourceType.BONES,
        List.of(() -> new TribeSigil("Reptile"))),

    // ----- Miscellaneous (no tribe) -----

    MOLE_MAN("Mole Man", 0, 6, 1, ResourceType.BLOOD, List.of(MightyLeapSigil::new, BurrowerSigil::new)),

    AMALGAM("Amalgam", 3, 3, 2, ResourceType.BLOOD, List.of()),

    PACK_RAT("Pack Rat", 2, 2, 2, ResourceType.BLOOD, List.of(TrinketBearerSigil::new)),

    URAYULI("Urayuli", 7, 7, 4, ResourceType.BLOOD, List.of()),

    CAT("Cat", 0, 1, 1, ResourceType.BLOOD, List.of(ManyLivesSigil::new)),

    MOLE("Mole", 0, 4, 1, ResourceType.BLOOD, List.of(BurrowerSigil::new)),

    RIVER_OTTER("River Otter", 1, 1, 1, ResourceType.BLOOD, List.of(WaterborneSigil::new)),

    SKUNK("Skunk", 0, 3, 1, ResourceType.BLOOD, List.of(StinkySigil::new)),

    WARREN("Warren", 0, 2, 1, ResourceType.BLOOD, List.of(RabbitHoleSigil::new)),

    BEAVER("Beaver", 1, 3, 1, ResourceType.BLOOD, List.of(DamBuilderSigil::new)),

    FIELD_MICE("Field Mice", 2, 2, 2, ResourceType.BLOOD, List.of(FecunditySigil::new)),

    RAT_KING("Rat King", 2, 1, 2, ResourceType.BLOOD, List.of(BoneKingSigil::new)),

    GREAT_WHITE("Great White", 4, 2, 3, ResourceType.BLOOD, List.of(WaterborneSigil::new)),

    GRIZZLY("Grizzly", 4, 6, 3, ResourceType.BLOOD, List.of()),

    OPOSSUM("Opossum", 1, 1, 2, ResourceType.BONES, List.of()),

    BAT("Bat", 2, 1, 4, ResourceType.BONES, List.of(AirborneSigil::new)),

    // ----- Obstacles (spawned by sigils, not drawn/paid for like ordinary
    // cards - cost is 0 since nobody ever pays it directly) -----

    DAM("Dam", 0, 2, 0, ResourceType.BLOOD, List.of(), true);

    // Add new ordinary cards as one line above - name, attack, health, cost,
    // costType, then a list of Sigil suppliers (identity tags and/or
    // abilities). Leave the list empty for a plain vanilla card.

    private final String cardName;
    private final int attack;
    private final int health;
    private final int cost;
    private final ResourceType costType;
    private final List<Supplier<Sigil>> sigilFactories;
    private final boolean isObstacle;

    CardType(String cardName, int attack, int health, int cost, ResourceType costType,
             List<Supplier<Sigil>> sigilFactories) {
        this(cardName, attack, health, cost, costType, sigilFactories, false);
    }

    /** Used only by obstacle entries (see Dam) - builds an ObstacleCard instead of a plain Card. */
    CardType(String cardName, int attack, int health, int cost, ResourceType costType,
             List<Supplier<Sigil>> sigilFactories, boolean isObstacle) {
        this.cardName = cardName;
        this.attack = attack;
        this.health = health;
        this.cost = cost;
        this.costType = costType;
        this.sigilFactories = sigilFactories;
        this.isObstacle = isObstacle;
    }

    /**
     * Builds a fresh Card instance for this catalog entry. Each call gets its
     * own independent Sigil objects (important - sigils like Diver carry
     * per-card state, so two of the same card must not share one sigil
     * instance). An obstacle entry builds an ObstacleCard instead of a plain
     * Card - a real subclass, not just attack=0 by convention.
     */
    public Card create() {
        Card card = isObstacle
            ? new ObstacleCard(cardName, health, cost, costType)
            : new Card(cardName, attack, health, cost, costType);
        card.sourceType = this;
        for (Supplier<Sigil> factory : sigilFactories) {
            card.addSigil(factory.get());
        }
        return card;
    }
}
