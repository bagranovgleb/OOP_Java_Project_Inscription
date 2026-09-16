package com.inscription.totem;

/**
 * A single totem: a stack of pieces all belonging to one tribe, granting +1
 * attack per stacked piece to any creature of that tribe still in play.
 * Computed on demand by GameEngine's effective-attack pipeline - the same
 * pattern as Leader, Stinky, and the Ant swarm mechanic - rather than
 * mutating a card's stored stats, so nothing needs to be "undone" if the
 * totem's piece count ever changed after cards were already placed.
 */
public class Totem {

    private final String tribe;
    private int pieceCount;

    public Totem(String tribe) {
        this.tribe = tribe;
        this.pieceCount = 1;
    }

    public String getTribe() {
        return tribe;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public void addPiece() {
        pieceCount++;
    }

    /** +1 attack per stacked piece. */
    public int getAttackBonus() {
        return pieceCount;
    }
}
