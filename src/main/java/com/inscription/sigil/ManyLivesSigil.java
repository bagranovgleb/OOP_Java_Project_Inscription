package com.inscription.sigil;

/**
 * When this card is sacrificed, it does not perish - the owner still gains
 * the usual resources, but the card stays alive on the board. A passive
 * marker: Player.sacrificeCard and GameEngine.sacrificeFromBoard check for
 * it by name rather than this class doing anything reactively.
 */
public class ManyLivesSigil extends PassiveMarkerSigil {

    @Override
    public String getName() {
        return "Many Lives";
    }
}
