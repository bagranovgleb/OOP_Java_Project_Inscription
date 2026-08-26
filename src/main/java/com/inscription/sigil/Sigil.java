package com.inscription.sigil;

import com.inscription.board.Slot;
import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.player.Player;

/**
 * A special ability attached to a card. The engine and Card class never check
 * "is this a SharpQuills card?" - they just call apply() on whatever sigils are
 * present, so new sigils can be added without touching existing code.
 */
public interface Sigil {

    /** React to a game event (attack, death, turn start, ...). */
    void apply(GameEvent event, Card owner, GameContext context);

    String getName();

    /**
     * Whether this sigil currently prevents its owner from being targeted
     * (e.g. a submerged Diver). Default: never blocks targeting.
     */
    default boolean preventsTargeting(Card owner) {
        return false;
    }

    /**
     * Identity sigils (e.g. "Squirrel") mark what a card fundamentally is so
     * totem-style modifiers can check for them generically via
     * {@code Card.hasSigil(name)} instead of an instanceof chain - but they carry
     * no active effect and shouldn't clutter the card's UI. Default: shown normally.
     */
    default boolean isHidden() {
        return false;
    }

    /**
     * Finds which player currently has this exact card on their side of the
     * board, by scanning both sides for a reference match. Sigils that need
     * to add something to "their own" player's hand (Unkillable, Hoarder)
     * use this rather than the engine threading ownership through every
     * event - a card doesn't otherwise know which side it's on.
     * Returns null if the card isn't currently on either side of the board.
     */
    default Player ownerOf(Card card, GameContext context) {
        for (Slot slot : context.getBoard().getPlayerSlots()) {
            if (slot.getOccupant() == card) {
                return context.getPlayer();
            }
        }
        for (Slot slot : context.getBoard().getOpponentSlots()) {
            if (slot.getOccupant() == card) {
                return context.getOpponent();
            }
        }
        return null;
    }

    /**
     * Finds where a card currently sits on the board: {1, lane} for the
     * player's side, {0, lane} for the opponent's, or null if it's not
     * currently placed. Used by sigils that need to act on their own board
     * position (e.g. Frozen Away replacing itself in the same lane).
     */
    default int[] locationOf(Card card, GameContext context) {
        var playerSlots = context.getBoard().getPlayerSlots();
        for (int i = 0; i < playerSlots.length; i++) {
            if (playerSlots[i].getOccupant() == card) {
                return new int[]{1, i};
            }
        }
        var opponentSlots = context.getBoard().getOpponentSlots();
        for (int i = 0; i < opponentSlots.length; i++) {
            if (opponentSlots[i].getOccupant() == card) {
                return new int[]{0, i};
            }
        }
        return null;
    }

    /**
     * Attempts to move a card one lane in the given direction (+1 right, -1
     * left), staying on the same side of the board. Does nothing if the card
     * isn't currently placed, the target lane would be off the board, or the
     * target lane is already occupied - a blocked or edge-of-board move is
     * simply skipped, not an error. Used by Sprinter and Hefty.
     */
    default void tryMove(Card card, GameContext context, int direction) {
        int[] location = locationOf(card, context);
        if (location == null) {
            return;
        }
        boolean isPlayerSide = location[0] == 1;
        int lane = location[1];
        var board = context.getBoard();
        int laneCount = board.getPlayerSlots().length;
        int targetLane = lane + direction;
        if (targetLane < 0 || targetLane >= laneCount) {
            return;
        }
        if (board.peekCard(isPlayerSide, targetLane) != null) {
            return;
        }
        board.removeCard(isPlayerSide, lane);
        board.placeCard(isPlayerSide, targetLane, card);
    }
}
