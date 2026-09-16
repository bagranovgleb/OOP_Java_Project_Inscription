package com.inscription.sigil;

import com.inscription.board.Board;
import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/**
 * At the end of the owner's turn, this card advances one lane in its
 * current direction, pushing whatever's directly in its path one further
 * lane in that same direction to make room - not the whole row, just the
 * one card actually standing in the way. If there's no room to push that
 * card into (edge of board, or the lane beyond it is also occupied), this
 * direction is blocked - the sigil permanently reverses and tries the
 * opposite direction instead. If both directions are blocked, nothing
 * moves this turn.
 */
public class HeftySigil implements Sigil {

    public static final int LEFT = -1;
    public static final int RIGHT = 1;

    private int direction;

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
        Board board = context.getBoard();
        int laneCount = board.getPlayerSlots().length;

        if (!canAdvance(board, isPlayerSide, lane, laneCount, direction)) {
            direction = -direction; // this way is blocked - reverse and try the other way instead
            if (!canAdvance(board, isPlayerSide, lane, laneCount, direction)) {
                return; // blocked both ways - stays put entirely this turn
            }
        }

        int targetLane = lane + direction;
        Card inTheWay = board.peekCard(isPlayerSide, targetLane);
        if (inTheWay != null) {
            tryMove(inTheWay, context, direction); // canAdvance() already confirmed this has room
        }
        tryMove(owner, context, direction);
    }

    /** Whether the card at 'lane' could actually advance one step in 'direction' - either the next lane is free, or it's occupied but that one occupant could itself be pushed one further (the same depth of pushing this sigil has always supported). */
    private boolean canAdvance(Board board, boolean isPlayerSide, int lane, int laneCount, int dir) {
        int targetLane = lane + dir;
        if (targetLane < 0 || targetLane >= laneCount) {
            return false;
        }
        if (board.peekCard(isPlayerSide, targetLane) == null) {
            return true;
        }
        int beyondLane = targetLane + dir;
        return beyondLane >= 0 && beyondLane < laneCount && board.peekCard(isPlayerSide, beyondLane) == null;
    }

    @Override
    public String getName() {
        return "Hefty";
    }
}
