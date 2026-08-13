package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;

/**
 * This card blocks opposing Airborne creatures - normally Airborne skips
 * the defender entirely, but a card with Mighty Leap still intercepts it.
 * A passive marker: GameEngine.resolveLaneAttack checks for it by name.
 */
public class MightyLeapSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine, not reactive.
    }

    @Override
    public String getName() {
        return "Mighty Leap";
    }
}
