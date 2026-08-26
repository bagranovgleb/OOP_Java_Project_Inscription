package com.inscription.ai;

import com.inscription.board.Board;
import com.inscription.board.Slot;
import com.inscription.engine.GameEngine;
import com.inscription.model.Card;
import com.inscription.model.DeckType;
import com.inscription.player.Player;

import java.util.NoSuchElementException;

/**
 * Minimal scripted opponent: draws, sacrifices a squirrel already on its own
 * board for resources if it can't currently afford anything else in hand,
 * plays the first affordable card into the first open slot, then rings its
 * own bell. Good enough to make the console prototype playable; a smarter AI
 * can implement OpponentAI later without changing anything that calls it.
 */
public class SimpleOpponentAI implements OpponentAI {

    @Override
    public void takeTurn(GameEngine engine, Board board, Player self, Player opponent) {
        draw(self);
        sacrificeSquirrelIfBlocked(engine, board, self);
        playFirstAffordableCardIntoOpenSlot(engine, self, board);
        engine.ringBell(false);
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
     * If there's a card in hand the AI can't currently afford, sacrifice one
     * squirrel already on its own board to work toward affording it - mirrors
     * the sacrifice-then-play pattern a human player would use. Sacrifice only
     * ever pulls from the board, never from hand.
     */
    private void sacrificeSquirrelIfBlocked(GameEngine engine, Board board, Player self) {
        boolean hasUnaffordableCard = self.getHand().stream().anyMatch(card -> !canAfford(self, card));
        if (!hasUnaffordableCard) {
            return;
        }
        Slot[] slots = board.getOpponentSlots();
        for (int i = 0; i < slots.length; i++) {
            Card occupant = slots[i].getOccupant();
            if (occupant != null && occupant.hasSigil("Squirrel")) {
                engine.sacrificeFromBoard(false, i);
                return;
            }
        }
    }

    private void playFirstAffordableCardIntoOpenSlot(GameEngine engine, Player self, Board board) {
        int emptyLane = firstEmptyLane(board);
        if (emptyLane == -1) {
            return;
        }
        for (Card card : self.getHand()) {
            if (canAfford(self, card)) {
                if (card.getCost() > 0) {
                    self.spendResource(card.getCostType(), card.getCost());
                }
                self.removeFromHand(card);
                engine.placeCard(false, emptyLane, card);
                return;
            }
        }
    }

    private boolean canAfford(Player self, Card card) {
        if (card.getCost() == 0) {
            return true;
        }
        int available = switch (card.getCostType()) {
            case BLOOD -> self.getBlood();
            case BONES -> self.getBones();
        };
        return available >= card.getCost();
    }

    private int firstEmptyLane(Board board) {
        Slot[] slots = board.getOpponentSlots();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
