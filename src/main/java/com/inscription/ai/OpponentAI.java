package com.inscription.ai;

import com.inscription.board.Board;
import com.inscription.engine.GameEngine;
import com.inscription.player.Player;
import com.inscription.ui.GameUI;

/**
 * Decides and executes the opponent's draw and card placements for one
 * turn - NOT the attack itself. The console loop (or later, the JavaFX
 * controller) calls takeTurn() to let the opponent act, shows the player
 * what happened, and only then rings the bell for combat - so newly placed
 * cards are visible before they attack, not "played and attacking" in one
 * invisible step.
 * <p>
 * It doesn't know or care whether the implementation is a simple scripted
 * routine or something smarter. Swapping AI difficulty later means adding a
 * new class, not touching the game loop.
 */
public interface OpponentAI {
    void takeTurn(GameEngine engine, Board board, Player self, Player opponent, GameUI ui);
}
