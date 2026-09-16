package com.inscription.sigil;


/**
 * This card will move to any empty space that's attacked by an enemy, to
 * block it. A passive marker: GameEngine.resolveLaneAttack checks for it by
 * name and does the actual relocation, since a sigil can't move its own
 * card between board slots on its own.
 */
public class BurrowerSigil extends PassiveMarkerSigil {

    @Override
    public String getName() {
        return "Burrower";
    }
}
