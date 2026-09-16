package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/**
 * A temporary grant of Airborne, lasting only until the owner's next
 * TURN_START - used by the Harpie's Birdleg Fan item, which affects only
 * creatures already on the board at the moment it's used, for the rest of
 * that turn only. Shares Airborne's exact name, so every existing
 * hasSigil("Airborne") check throughout the engine (the attack-bypass
 * logic, Mighty Leap, ...) treats it identically to the real thing - but it
 * removes itself the next time its owner's side starts a turn.
 * <p>
 * This relies on the same timing guarantee used elsewhere (e.g. Fledgling):
 * a card already on the board mid-turn won't receive another TURN_START
 * until its owner's side begins its next turn, so "the next TURN_START this
 * card sees" reliably means "the turn after the one it was granted in."
 */
public class TemporaryAirborneSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() == GameEventType.TURN_START && event.getSource() == owner) {
            owner.removeSigil(this);
        }
    }

    @Override
    public String getName() {
        return "Airborne";
    }
}
