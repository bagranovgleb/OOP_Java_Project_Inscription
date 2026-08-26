package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * When this card dies, 4 bones are awarded instead of 1. A passive marker:
 * GameEngine's bone-crediting step checks for it by name rather than this
 * class doing anything reactively.
 */
public class BoneKingSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine.
    }

    @Override
    public String getName() {
        return "Bone King";
    }
}
