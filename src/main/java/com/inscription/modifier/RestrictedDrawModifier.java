package com.inscription.modifier;

import com.inscription.engine.GameContext;

/** Reduces how many cards the player draws per turn. */
public class RestrictedDrawModifier implements ChallengeModifier {

    private final int drawReduction;

    public RestrictedDrawModifier(int drawReduction) {
        this.drawReduction = drawReduction;
    }

    public int getDrawReduction() {
        return drawReduction;
    }

    @Override
    public void apply(GameContext context) {
        // No board-wide effect on its own; the engine consults getDrawReduction()
        // during the draw phase.
    }

    @Override
    public boolean isCompatibleWith(ChallengeModifier other) {
        return !(other instanceof LimitedSacrificesModifier);
    }

    @Override
    public String getName() {
        return "Restricted Draw";
    }
}
