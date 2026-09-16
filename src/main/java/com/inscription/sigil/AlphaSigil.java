package com.inscription.sigil;


/**
 * Creatures adjacent to this card (same side, one lane over) gain 1 power -
 * not this card itself. A passive, position-based marker: checked by name in
 * GameEngine.computeEffectiveAttack rather than reacting to an event, since
 * the effect depends on current board position, not something that happened.
 */
public class AlphaSigil extends PassiveMarkerSigil {

    @Override
    public String getName() {
        return "Alpha";
    }
}
