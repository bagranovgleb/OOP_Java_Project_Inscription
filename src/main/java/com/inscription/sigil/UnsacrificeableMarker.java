package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;

/**
 * A hidden marker with no active effect - marks a card (Boulder, Frozen
 * Opossum) as one that can never be voluntarily sacrificed by its owner,
 * checked by GameEngine.sacrificeFromBoard(). This is a different
 * restriction from Many Lives: a Many Lives card CAN be sacrificed and
 * simply survives it (still granting blood/bones); a card with this marker
 * refuses the sacrifice attempt entirely. It can still die normally in
 * combat - only the player-initiated sacrifice action is blocked.
 */
public class UnsacrificeableMarker implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        // No active effect - marker only.
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public String getName() {
        return "Unsacrificeable";
    }
}
