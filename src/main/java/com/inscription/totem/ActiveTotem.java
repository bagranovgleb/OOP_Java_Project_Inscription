package com.inscription.totem;

import com.inscription.sigil.Sigil;

/**
 * The player's one constructed, active totem - a head (tribe) paired with a
 * body (granted sigil), assembled from held inventory pieces. Neither piece
 * is consumed by construction - they stay in inventory, so a later
 * Woodcarver visit can freely reconfigure using any held combination.
 */
public class ActiveTotem {

    private final TotemHead head;
    private final TotemBody body;

    public ActiveTotem(TotemHead head, TotemBody body) {
        this.head = head;
        this.body = body;
    }

    public String getTribe() {
        return head.getTribe();
    }

    public String getGrantedSigilName() {
        return body.getSigilName();
    }

    /** A fresh instance of the granted sigil, for whatever creature just triggered it. */
    public Sigil createGrantedSigil() {
        return body.createSigil();
    }

    @Override
    public String toString() {
        return "Totem [" + head.getTribe() + " tribe grants " + body.getSigilName() + "]";
    }
}
