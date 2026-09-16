package com.inscription.ai;

import com.inscription.board.Board;
import com.inscription.board.ReadOnlySlot;
import com.inscription.engine.GameEngine;
import com.inscription.model.Card;
import com.inscription.model.DeckType;
import com.inscription.player.Player;
import com.inscription.ui.GameUI;

import java.util.NoSuchElementException;

/**
 * Minimal scripted opponent: draws, then plays up to a few cards this turn
 * (see playCards()), then rings its own bell.
 * <p>
 * Unlike the player, the opponent doesn't pay blood/bone costs for its
 * cards - matching how Leshy plays in the real game, unconstrained by the
 * player's resource economy. This is also a practical necessity here: this
 * project's opponents carry no Squirrels at all (see RunGame's deck pools),
 * and sacrifice-for-resources only works once something is already on the
 * board - with zero starting resources and zero squirrels, an opponent that
 * had to pay would never be able to place its very first card at all.
 * <p>
 * Two deliberate departures from the real game's rules, both aimed at
 * keeping fights winnable: it plays at most 2 new cards per turn (not 3),
 * and it can only ever place a NEW card into reserve, never directly into
 * the front row - so every card spends one full turn "in preparation"
 * before it can attack. Reserve cards promote to the front automatically
 * at the start of the opponent's own next turn (see Battle.opponentTurn()
 * and GameEngine.promoteReserveCards()) and are NOT exhausted when they do
 * - they can attack immediately once promoted. The player's own cards are
 * completely unaffected by any of this and can still attack the same turn
 * they're placed, matching the real game and preserving the player's own
 * tactics.
 * <p>
 * Good enough to make the console prototype playable; a smarter AI can
 * implement OpponentAI later without changing anything that calls it.
 */
public class SimpleOpponentAI implements OpponentAI {

    /** Total cards across the front and reserve rows combined - matches the board's own actual capacity (4 + 4). */
    private static final int MAX_CARDS_IN_PLAY = 6;

    @Override
    public void takeTurn(GameEngine engine, Board board, Player self, Player opponent, GameUI ui) {
        draw(self);
        playCards(engine, self, board, ui);
    }

    private void draw(Player self) {
        try {
            self.drawFromDeck(DeckType.ANIMAL);
        } catch (NoSuchElementException animalDeckEmpty) {
            try {
                self.drawFromDeck(DeckType.SQUIRREL);
            } catch (NoSuchElementException bothDecksEmpty) {
                // Nothing left to draw this turn.
            }
        }
    }

    /**
     * Plays up to a few cards this turn, tapering off as the opponent's own
     * board fills up: more aggressive early (up to 2 with 0-3 cards in
     * play), more conservative as it approaches a cap of 6 total (matching
     * the board's actual capacity) - so it builds up a presence without
     * simply flooding the board every single turn.
     */
    private void playCards(GameEngine engine, Player self, Board board, GameUI ui) {
        int played = 0;
        int maxThisTurn = maxCardsToPlayThisTurn(countCardsInPlay(board.getOpponentSlots(), board.getOpponentReserveSlots()));
        while (played < maxThisTurn) {
            int aiCount = countCardsInPlay(board.getOpponentSlots(), board.getOpponentReserveSlots());
            if (aiCount >= MAX_CARDS_IN_PLAY) {
                break;
            }
            if (!playOneCardIntoReserve(engine, self, board, ui)) {
                break; // nothing left in hand, or no reserve room left
            }
            played++;
        }
    }

    /** Fewer new cards as the board fills up, tapering toward the cap rather than hitting it abruptly. */
    private int maxCardsToPlayThisTurn(int currentCount) {
        if (currentCount <= 3) {
            return 2;
        }
        return 1;
    }

    private int countCardsInPlay(ReadOnlySlot[]... slotGroups) {
        int count = 0;
        for (ReadOnlySlot[] slots : slotGroups) {
            for (ReadOnlySlot slot : slots) {
                if (!slot.isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Places a new card into reserve - never directly into the front row.
     * Returns whether a card was actually placed. No cost is paid - see the
     * class-level note on why.
     */
    private boolean playOneCardIntoReserve(GameEngine engine, Player self, Board board, GameUI ui) {
        if (self.getHand().isEmpty()) {
            return false;
        }
        int emptyReserveLane = firstEmptyLane(board.getOpponentReserveSlots());
        if (emptyReserveLane == -1) {
            return false;
        }
        Card card = self.getHand().get(0);
        self.removeFromHand(card);
        engine.placeReserveCard(emptyReserveLane, card);
        ui.show("Opponent readies " + card.getName() + " in reserve, lane " + emptyReserveLane + ".");
        return true;
    }

    private int firstEmptyLane(ReadOnlySlot[] slots) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
