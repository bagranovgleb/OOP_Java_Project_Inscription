package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/**
 * At the end of the owner's turn, this card moves one lane in a fixed
 * direction. The direction is a constructor parameter, not something the
 * sigil infers - e.g. {@code new SprinterSigil(SprinterSigil.RIGHT)}.
 */
public class SprinterSigil implements Sigil {

    public static final int LEFT = -1;
    public static final int RIGHT = 1;

    private final int direction;

    public SprinterSigil(int direction) {
        this.direction = direction;
    }

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.TURN_END || event.getSource() != owner) {
            return;
        }
        tryMove(owner, context, direction);
    }

    @Override
    public String getName() {
        return "Sprinter";
    }
}
