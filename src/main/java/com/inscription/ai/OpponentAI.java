package com.inscription.ai;

import com.inscription.board.Board;
import com.inscription.engine.GameEngine;
import com.inscription.player.Player;

/**
 * Decides and executes the opponent's actions for one turn. The console loop
 * (or later, the JavaFX controller) only ever calls takeTurn() - it doesn't
 * know or care whether the implementation is a simple scripted routine or
 * something smarter. Swapping AI difficulty later means adding a new class,
 * not touching the game loop.
 */
public interface OpponentAI {
    void takeTurn(GameEngine engine, Board board, Player self, Player opponent);
}
