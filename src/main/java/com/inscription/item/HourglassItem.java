package com.inscription.item;

/**
 * Hourglass: the opponent's next turn is skipped entirely - no cards
 * played, no attacks, as though the turn never happened. See
 * Battle.opponentTurn(), which checks and consumes this request via
 * GameEngine before doing anything else on the opponent's turn.
 */
public class HourglassItem implements Item {

    @Override
    public String getName() {
        return "Hourglass";
    }

    @Override
    public String getDescription() {
        return "The opponent's next turn is skipped entirely.";
    }

    @Override
    public boolean use(ItemContext context) {
        context.getEngine().requestSkipOpponentNextTurn();
        context.getUI().show("Sand begins to fall - the opponent's next turn will be skipped entirely.");
        return true;
    }
}
