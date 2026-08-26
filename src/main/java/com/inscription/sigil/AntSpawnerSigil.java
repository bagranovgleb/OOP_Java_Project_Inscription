package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;
import com.inscription.player.Player;

/** When this card is played, an Ant (Worker Ant) is created in the owner's hand. */
public class AntSpawnerSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.PLACE || event.getSource() != owner) {
            return;
        }
        Player controllingPlayer = ownerOf(owner, context);
        if (controllingPlayer != null) {
            controllingPlayer.addToHand(CardType.WORKER_ANT.create());
        }
    }

    @Override
    public String getName() {
        return "Ant Spawner";
    }
}
