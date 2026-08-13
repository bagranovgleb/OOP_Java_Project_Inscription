package com.inscription.modifier;

import com.inscription.engine.GameContext;

/**
 * An optional difficulty modifier the player can pick before a run.
 * The engine applies whatever modifiers are active through this interface
 * without needing to know how many there are or which concrete ones.
 */
public interface ChallengeModifier {

    void apply(GameContext context);

    /** Whether this modifier can be active at the same time as {@code other}. */
    boolean isCompatibleWith(ChallengeModifier other);

    String getName();
}
