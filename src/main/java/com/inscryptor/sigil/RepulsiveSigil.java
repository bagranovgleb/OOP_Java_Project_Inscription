package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;

/**
 * If a creature would attack this card, it does not - the attack fizzles
 * entirely, not even landing on the defending player. Different from
 * Waterborne, which redirects the attack to the face; Repulsive cancels it
 * outright. A passive marker checked by name in GameEngine.resolveLaneAttack.
 */
public class RepulsiveSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine, not reactive.
    }

    @Override
    public String getName() {
        return "Repulsive";
    }
}
