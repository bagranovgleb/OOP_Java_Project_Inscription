package com.inscription.sigil;


/**
 * This card strikes each opposing space to the left and right of the space
 * directly across from it - not that space itself. A passive marker:
 * GameEngine.resolveLaneAttack checks for it by name to pick target lanes.
 */
public class BifurcatedStrikeSigil extends PassiveMarkerSigil {

    @Override
    public String getName() {
        return "Bifurcated Strike";
    }
}
