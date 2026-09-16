package com.inscription.run;

/**
 * What happens when the player arrives at a path node - a battle or an
 * event. Abstraction: PathNode and PathRunner never need to know which kind
 * of content a node holds, only that it can describe itself and resolve.
 */
public interface NodeContent {

    /** Shown to the player when they arrive at, or are choosing between, this node. */
    String describe();

    /**
     * Executes this node's effect. Returns false only to mean "the run ends
     * here" (a lost or abandoned battle) - an event always returns true,
     * since nothing about resolving an event ends the run.
     */
    boolean resolve(RunContext context);
}
