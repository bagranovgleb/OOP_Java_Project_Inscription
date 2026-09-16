package com.inscription.ui;

import java.util.List;

/**
 * A structured, UI-agnostic snapshot of the board at one instant - what's
 * described in each lane, on each side, as plain per-slot text (or
 * "(empty)"). Exists so a GameUI implementation that wants a real visual
 * board (a persistent grid of lane boxes, updated in place) can render one
 * directly from structured data, rather than needing to parse that
 * information back out of a formatted text block meant for a console.
 * ConsoleGameUI still renders this as its usual text table - nothing
 * changes there - while a graphical implementation can lay it out as an
 * actual grid instead of appending another wall of text to a scrolling log
 * every single turn.
 * <p>
 * Each list is one entry per lane, already in lane order (index 0 = lane
 * 0, and so on) - the caller building this (Battle) is the one place that
 * already knows the board's actual layout, so this class stays a plain,
 * passive data holder rather than needing its own board-reading logic.
 */
public final class BoardSnapshot {

    private final List<String> playerLanes;
    private final List<String> opponentAttackingLanes;
    private final List<String> opponentReserveLanes;

    public BoardSnapshot(List<String> playerLanes, List<String> opponentAttackingLanes, List<String> opponentReserveLanes) {
        this.playerLanes = playerLanes;
        this.opponentAttackingLanes = opponentAttackingLanes;
        this.opponentReserveLanes = opponentReserveLanes;
    }

    public List<String> getPlayerLanes() {
        return playerLanes;
    }

    public List<String> getOpponentAttackingLanes() {
        return opponentAttackingLanes;
    }

    public List<String> getOpponentReserveLanes() {
        return opponentReserveLanes;
    }

    public int getLaneCount() {
        return playerLanes.size();
    }
}
