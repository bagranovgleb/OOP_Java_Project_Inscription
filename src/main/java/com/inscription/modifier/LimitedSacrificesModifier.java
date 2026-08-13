package com.inscription.modifier;

import com.inscription.engine.GameContext;

/**
 * Caps how many creatures the player may sacrifice per turn. Enforcement lives
 * in GameEngine, which checks getMaxSacrificesPerTurn() before allowing a sacrifice.
 */
public class LimitedSacrificesModifier implements ChallengeModifier {

    private final int maxSacrificesPerTurn;

    public LimitedSacrificesModifier(int maxSacrificesPerTurn) {
        this.maxSacrificesPerTurn = maxSacrificesPerTurn;
    }

    public int getMaxSacrificesPerTurn() {
        return maxSacrificesPerTurn;
    }

    @Override
    public void apply(GameContext context) {
        // No board-wide effect on its own; the engine consults getMaxSacrificesPerTurn().
    }

    @Override
    public boolean isCompatibleWith(ChallengeModifier other) {
        // Deemed too punishing alongside a restricted draw in this ruleset.
        return !(other instanceof RestrictedDrawModifier);
    }

    @Override
    public String getName() {
        return "Limited Sacrifices";
    }
}
