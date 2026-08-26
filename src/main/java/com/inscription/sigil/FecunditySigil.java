package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;
import com.inscription.player.Player;

/**
 * When this card is played, a copy of it enters the owner's hand. Uses
 * Card.getSourceType() to build the copy - same technique as Unkillable.
 */
public class FecunditySigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.PLACE || event.getSource() != owner) {
            return;
        }
        CardType sourceType = owner.getSourceType();
        if (sourceType == null) {
            return;
        }
        Player controllingPlayer = ownerOf(owner, context);
        if (controllingPlayer != null) {
            controllingPlayer.addToHand(sourceType.create());
        }
    }

    @Override
    public String getName() {
        return "Fecundity";
    }
}
