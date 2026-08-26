package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * When an opposing card is played opposite an empty space, this card moves
 * to that space. A passive marker: this has to react to something happening
 * on the OTHER side of the board while sitting elsewhere on its own side,
 * which nothing in the event system supports for a card that isn't the one
 * being placed - so GameEngine checks for it by name directly, right after
 * any card is played, rather than this class reacting to anything itself.
 */
public class GuardianSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // Passive - checked by name in GameEngine.placeCard().
    }

    @Override
    public String getName() {
        return "Guardian";
    }
}
