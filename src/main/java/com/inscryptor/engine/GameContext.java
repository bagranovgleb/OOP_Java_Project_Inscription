package com.inscryptor.engine;

import com.inscryptor.board.Board;
import com.inscryptor.player.Player;

/**
 * A small read-only carrier passed to sigils and modifiers so they can see
 * (and act on) the board and both players without the engine exposing its
 * full internal state piecemeal through getters.
 */
public final class GameContext {

    private final Board board;
    private final Player player;
    private final Player opponent;

    public GameContext(Board board, Player player, Player opponent) {
        this.board = board;
        this.player = player;
        this.opponent = opponent;
    }

    public Board getBoard() {
        return board;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getOpponent() {
        return opponent;
    }
}
