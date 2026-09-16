package com.inscription.item;

import com.inscription.board.Board;
import com.inscription.engine.GameEngine;
import com.inscription.player.Player;
import com.inscription.ui.ConsoleGameUI;
import com.inscription.ui.GameUI;

import java.util.Scanner;

/**
 * What an item needs to use itself mid-battle: the engine (so a placement
 * still fires PLACE correctly), the board, both players, and a GameUI for
 * anything needing to show information or collect a choice (e.g. "which
 * lane"). Same bundling reasoning as GameContext for sigils and RunContext
 * for run events.
 * <p>
 * The Scanner-based constructor wraps its Scanner in a ConsoleGameUI
 * automatically, so every existing call site keeps working unchanged -
 * only the item classes themselves needed to change, switching from
 * talking to System.out/Scanner directly to going through getUI() instead.
 */
public final class ItemContext {

    private final GameEngine engine;
    private final Board board;
    private final Player player;
    private final Player opponent;
    private final Scanner scanner;
    private final GameUI ui;

    public ItemContext(GameEngine engine, Board board, Player player, Player opponent, Scanner scanner) {
        this.engine = engine;
        this.board = board;
        this.player = player;
        this.opponent = opponent;
        this.scanner = scanner;
        this.ui = new ConsoleGameUI(scanner);
    }

    /** For a GUI context, which has no real Scanner to speak of - getScanner() returns null here. */
    public ItemContext(GameEngine engine, Board board, Player player, Player opponent, GameUI ui) {
        this.engine = engine;
        this.board = board;
        this.player = player;
        this.opponent = opponent;
        this.scanner = null;
        this.ui = ui;
    }

    public GameEngine getEngine() {
        return engine;
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

    public Scanner getScanner() {
        return scanner;
    }

    public GameUI getUI() {
        return ui;
    }
}
