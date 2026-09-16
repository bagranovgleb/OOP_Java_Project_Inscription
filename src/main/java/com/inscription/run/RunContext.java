package com.inscription.run;

import com.inscription.player.Player;
import com.inscription.ui.ConsoleGameUI;
import com.inscription.ui.GameUI;

import java.util.Scanner;

/**
 * What a path node needs to resolve itself: the run's one persistent Player
 * (deck and hand carry across the whole run) and a GameUI for anything that
 * needs to show information or collect a choice from the player. Kept as
 * one small bundle rather than threading two separate parameters through
 * every NodeContent, the same reasoning as GameContext bundling
 * board/player/opponent for sigils.
 * <p>
 * The Scanner-based constructor wraps its Scanner in a ConsoleGameUI
 * automatically, so every existing call site (RunGame, and the many
 * existing tests) keeps working completely unchanged - only the event
 * classes themselves needed to change, switching from talking to
 * System.out/Scanner directly to going through getUI() instead.
 */
public final class RunContext {

    private final Player player;
    private final Scanner scanner;
    private final GameUI ui;

    public RunContext(Player player, Scanner scanner) {
        this.player = player;
        this.scanner = scanner;
        this.ui = new ConsoleGameUI(scanner);
    }

    /** For a future GUI context, which has no real Scanner to speak of - getScanner() returns null here, meaning any code that still needs it directly hasn't been decoupled yet. */
    public RunContext(Player player, GameUI ui) {
        this.player = player;
        this.scanner = null;
        this.ui = ui;
    }

    public Player getPlayer() {
        return player;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public GameUI getUI() {
        return ui;
    }
}
