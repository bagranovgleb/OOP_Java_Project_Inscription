package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * If a card its owner controls dies in combat, this card is automatically
 * played from hand onto the now-empty space. A passive marker: this has to
 * react while sitting in hand, which nothing in the event system currently
 * supports (only board cards ever receive events) - so GameEngine checks for
 * it by name directly, right after clearing a combat death, rather than this
 * class reacting to anything itself.
 */
public class CorpseEaterSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine after a friendly combat death.
    }

    @Override
    public String getName() {
        return "Corpse Eater";
    }
}
