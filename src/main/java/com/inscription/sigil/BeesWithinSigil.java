package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;
import com.inscription.player.Player;

/**
 * When this card is struck (takes a direct hit in combat), a Bee is created
 * in the owner's hand. Reacts to ATTACK where this card is the target - only
 * fires for an attack that actually connects, not one that missed entirely
 * due to a bypass like Airborne.
 */
public class BeesWithinSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.ATTACK || event.getTarget() != owner) {
            return;
        }
        Player controllingPlayer = ownerOf(owner, context);
        if (controllingPlayer != null) {
            controllingPlayer.addToHand(CardType.BEE.create());
        }
    }

    @Override
    public String getName() {
        return "Bees Within";
    }
}
