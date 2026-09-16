package com.inscription.sigil;


/**
 * The creature directly opposing this card (same lane, other side) deals 1
 * less damage on every attack it makes, for as long as it's facing this
 * card - including every lane a multi-strike sigil (Bifurcated/Trifurcated)
 * hits that turn, not just a matchup against this card specifically. A
 * passive, position-based marker - checked by name in
 * GameEngine.computeEffectiveAttack rather than reacting to an event, since
 * the effect depends on current board position, not something that happened.
 * <p>
 * Simplification: this reduces the damage an attack deals. It doesn't
 * change what the opposing creature's own getAttack() reports, so a
 * reflect-style sigil on that creature (e.g. Sharp Quills) would still
 * reflect its full, un-reduced attack. A true continuous stat recalculation
 * would need more infrastructure than this one effect currently justifies.
 */
public class StinkySigil extends PassiveMarkerSigil {

    @Override
    public String getName() {
        return "Stinky";
    }
}
