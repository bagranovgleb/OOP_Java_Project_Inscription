package com.inscription.sigil;

import com.inscription.model.Card;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Sigils safe to hand out randomly - shared by anything that grants a
 * random bonus sigil (Build Totem's cap-fallback Amalgam, a Build Totem
 * body piece, the Trader's constructed Wolf-tier card). Anything needing
 * an explicit parameter (Fledgling's target form, Frozen Away's released
 * creature, Sprinter/Hefty's direction) is excluded, since there's no
 * sensible value to invent for those automatically.
 */
public final class RandomizableSigils {

    public static final List<Supplier<Sigil>> POOL = List.of(
        AirborneSigil::new, AntSpawnerSigil::new, BeesWithinSigil::new, BifurcatedStrikeSigil::new,
        BoneKingSigil::new, BurrowerSigil::new, CorpseEaterSigil::new, DamBuilderSigil::new,
        FecunditySigil::new, GuardianSigil::new, HoarderSigil::new,
        LeaderSigil::new, LooseTailSigil::new, ManyLivesSigil::new, MightyLeapSigil::new,
        RabbitHoleSigil::new, SharpQuillsSigil::new, StinkySigil::new, TouchOfDeathSigil::new,
        TrifurcatedStrikeSigil::new, TrinketBearerSigil::new, UnkillableSigil::new,
        WaterborneSigil::new, WorthySacrificeSigil::new);

    private RandomizableSigils() {
    }

    public static Sigil random(Random random) {
        return POOL.get(random.nextInt(POOL.size())).get();
    }

    /**
     * Same as random(), but never picks a sigil the given card already
     * carries - e.g. never rolls "Unkillable" onto an Ouroboros that
     * already has it as a base sigil, which would otherwise show up as a
     * visible, redundant duplicate (like "[Unkillable, Unkillable]").
     * Falls back to the plain random() only in the practically-impossible
     * case where every single randomizable sigil is already on the card.
     */
    public static Sigil randomExcluding(Random random, Card card) {
        List<Supplier<Sigil>> eligible = POOL.stream()
            .filter(factory -> !card.hasSigil(factory.get().getName()))
            .toList();
        if (eligible.isEmpty()) {
            return random(random);
        }
        return eligible.get(random.nextInt(eligible.size())).get();
    }
}
