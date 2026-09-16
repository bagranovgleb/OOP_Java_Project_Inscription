package com.inscription.sigil;


/**
 * When this card would be struck, a tail is created in its place and this
 * card escapes one lane to the right. A passive marker: the interception has
 * to happen BEFORE damage is dealt, which nothing in the reactive event
 * system supports (sigils only ever see an ATTACK after the fact) - so
 * GameEngine checks for it by name in strikeLane(), before resolveAttack()
 * is ever called, and does the actual swap-and-relocate itself.
 */
public class LooseTailSigil extends PassiveMarkerSigil {

    @Override
    public String getName() {
        return "Loose Tail";
    }
}
