package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

/** When this card is played, Dams are created on adjacent empty spaces. */
public class DamBuilderSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.PLACE || event.getSource() != owner) {
            return;
        }
        int[] location = locationOf(owner, context);
        if (location == null) {
            return;
        }
        boolean isPlayerSide = location[0] == 1;
        int lane = location[1];
        var board = context.getBoard();
        var slots = isPlayerSide ? board.getPlayerSlots() : board.getOpponentSlots();

        if (lane - 1 >= 0 && slots[lane - 1].getOccupant() == null) {
            board.placeCard(isPlayerSide, lane - 1, CardType.DAM.create());
        }
        if (lane + 1 < slots.length && slots[lane + 1].getOccupant() == null) {
            board.placeCard(isPlayerSide, lane + 1, CardType.DAM.create());
        }
    }

    @Override
    public String getName() {
        return "Dam Builder";
    }
}
