package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/**
 * At the end of the owner's turn, this and adjacent cards move one lane in a
 * fixed direction. Order matters here: to shift a contiguous group without
 * cards blocking each other, whichever card is already leading in the
 * direction of travel must move first to clear space - moving right, the
 * right neighbor goes first; moving left, the left neighbor goes first.
 */
public class HeftySigil implements Sigil {

    public static final int LEFT = -1;
    public static final int RIGHT = 1;

    private final int direction;

    public HeftySigil(int direction) {
        this.direction = direction;
    }

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.TURN_END || event.getSource() != owner) {
            return;
        }
        int[] location = locationOf(owner, context);
        if (location == null) {
            return;
        }
        boolean isPlayerSide = location[0] == 1;
        int lane = location[1];
        var slots = isPlayerSide ? context.getBoard().getPlayerSlots() : context.getBoard().getOpponentSlots();
        Card left = (lane - 1 >= 0) ? slots[lane - 1].getOccupant() : null;
        Card right = (lane + 1 < slots.length) ? slots[lane + 1].getOccupant() : null;

        if (direction > 0) {
            if (right != null) {
                tryMove(right, context, direction);
            }
            tryMove(owner, context, direction);
            if (left != null) {
                tryMove(left, context, direction);
            }
        } else {
            if (left != null) {
                tryMove(left, context, direction);
            }
            tryMove(owner, context, direction);
            if (right != null) {
                tryMove(right, context, direction);
            }
        }
    }

    @Override
    public String getName() {
        return "Hefty";
    }
}
