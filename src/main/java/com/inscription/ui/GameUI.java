package com.inscription.ui;

import java.util.List;

/**
 * Abstraction for how the game shows information to the player and collects
 * a choice from them. Events, items, and (eventually) Battle itself decide
 * WHAT the options are and WHAT happens once one is picked - this interface
 * covers only HOW that conversation actually happens.
 * <p>
 * The console implementation (ConsoleGameUI) blocks on Scanner input,
 * matching this project's original behavior exactly. A future JavaFX
 * implementation would show real buttons and resume when one is clicked -
 * fundamentally different mechanics under the hood, but neither ever
 * requires a single line of change in any event or item class, since none
 * of them ever call System.out or Scanner directly anymore.
 */
public interface GameUI {

    /** Displays a line of information to the player - no response expected. */
    void show(String message);

    /**
     * Presents a numbered list of options under the given prompt, and
     * returns the index the player picked. Implementations are responsible
     * for only ever returning a valid index (0 to options.size() - 1) -
     * callers never need to re-validate the result.
     */
    int askChoice(String prompt, List<String> options);

    /**
     * Waits for a plain acknowledgment to continue - no options to choose
     * between, just "I'm ready to move on." Kept separate from askChoice
     * rather than modeled as a single-option choice, so a GUI can render
     * this as a plain "Continue" button rather than a one-item list.
     */
    void waitForContinue(String prompt);

    /**
     * Shows the current board state. Default implementation renders the
     * same text table Battle always has (kept here so ConsoleGameUI needs
     * no changes at all - it gets identical output to before, for free). A
     * GUI implementation that wants a real, persistent visual board
     * instead of another wall of scrolling text should override this and
     * ignore the default entirely.
     */
    default void updateBoard(BoardSnapshot snapshot) {
        StringBuilder sb = new StringBuilder("Board (lane: you | opponent (attacking) | opponent (next up)):");
        for (int i = 0; i < snapshot.getLaneCount(); i++) {
            String mine = snapshot.getPlayerLanes().get(i);
            String theirsAttacking = snapshot.getOpponentAttackingLanes().get(i);
            String theirsReserve = i < snapshot.getOpponentReserveLanes().size()
                ? snapshot.getOpponentReserveLanes().get(i) : "(n/a)";
            sb.append(String.format("%n  [%d] %-20s | %-20s | %-20s", i, mine, theirsAttacking, theirsReserve));
        }
        show(sb.toString());
    }

    /**
     * Signals that the fight is over and any persistent board display
     * should disappear - a battle's board is only relevant while that
     * specific fight is happening; leaving it visible during whatever map
     * event comes next would be a stale leftover. Default no-op: the
     * console has no persistent board widget to clear (updateBoard()
     * already just prints a line each time, nothing sticks around
     * regardless), so only a GUI implementation with a real, persistent
     * board area needs to override this.
     */
    default void clearBoard() {
    }

    /**
     * Shows the player's current bone count, live, as it changes. Default
     * implementation is a no-op - the console already shows bones once per
     * turn via the existing status line, so a separate live display there
     * would just be redundant, repeated noise. A GUI implementation with a
     * real, persistent bones display near the board should override this
     * and update that in place, the same way updateBoard() does for the
     * board itself.
     */
    default void updateBones(int amount) {
    }
}
