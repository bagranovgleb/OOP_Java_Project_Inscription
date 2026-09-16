package com.inscription.util;

import java.util.Random;

/**
 * A single shared entry point for constructing gameplay Random instances -
 * used by every run event that doesn't have a specific seed injected for
 * testing (their own no-arg constructors call this instead of `new
 * Random()` directly).
 * <p>
 * Exists to harden against a known weak spot in java.util.Random's own
 * no-arg constructor: its default seed is derived from System.nanoTime()
 * combined with a static counter that resets fresh every JVM process. In
 * one round of manual testing here (20 separate, quick-succession JVM
 * launches, each drawing exactly one value), that combination produced a
 * visibly skewed first draw - though a much larger batch (280 total
 * launches) came back statistically consistent with a fair, uniform
 * distribution, so this wasn't confirmed as a reliable, systematic bug.
 * Still, it's exactly the scenario a real player creates every time they
 * launch the game, play one run, and relaunch for another - each run
 * typically only draws a handful of times from an event's own Random, so
 * there's little room for an early correlation to average out within a
 * single run the way it would across thousands of test iterations.
 * <p>
 * Mixing in System.identityHashCode() on a freshly allocated object adds
 * entropy from the JVM's own object allocation/GC state, which is
 * independent of wall-clock timing - a simple, standard hardening step
 * that costs nothing and can only help, even though the underlying
 * concern couldn't be definitively reproduced at scale.
 */
public final class GameRandom {

    private GameRandom() {
    }

    public static Random create() {
        long seed = System.nanoTime() ^ ((long) System.identityHashCode(new Object()) << 32);
        return new Random(seed);
    }
}
