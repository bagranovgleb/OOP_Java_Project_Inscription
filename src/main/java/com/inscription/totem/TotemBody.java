package com.inscription.totem;

import com.inscription.sigil.Sigil;

import java.util.function.Supplier;

/**
 * A totem body - carries a specific sigil (chosen randomly when the piece
 * was granted) that gets attached to matching-tribe creatures once this
 * body is assembled into an active totem. Stores a factory rather than one
 * shared instance, so every creature that receives it gets its own fresh
 * sigil object - sharing one instance would be a real bug for any sigil
 * that carries per-card state (e.g. Fledgling's own-turn-count flag).
 */
public class TotemBody implements TotemPiece {

    private final Supplier<Sigil> sigilFactory;

    public TotemBody(Supplier<Sigil> sigilFactory) {
        this.sigilFactory = sigilFactory;
    }

    /** Builds a throwaway instance just to read its name - cheap, and avoids storing the name separately from the factory. */
    public String getSigilName() {
        return sigilFactory.get().getName();
    }

    /** A fresh instance of this body's sigil - call this once per card that should receive it. */
    public Sigil createSigil() {
        return sigilFactory.get();
    }

    @Override
    public String describe() {
        return "Body (" + getSigilName() + ")";
    }
}
