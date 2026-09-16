package com.inscription.sigil;


/**
 * This card will ignore opposing cards and strike the defending player
 * directly - unless the defender carries Mighty Leap. A passive marker: no
 * reactive behavior of its own. GameEngine.resolveLaneAttack checks for it
 * by name when resolving an attack.
 */
public class AirborneSigil extends PassiveMarkerSigil {

    @Override
    public String getName() {
        return "Airborne";
    }
}
