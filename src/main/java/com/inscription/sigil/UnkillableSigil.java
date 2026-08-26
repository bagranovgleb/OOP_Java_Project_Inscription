package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;
import com.inscription.player.Player;

/**
 * When this card perishes, a copy of it enters its owner's hand. Needs to
 * know which CardType it was built from (Card.getSourceType()) to make a
 * fresh copy, and which player controls it (found by scanning the board via
 * Sigil.ownerOf()) - a card ad hoc built outside the catalog, or one no
 * longer traceable to either side, simply can't trigger this.
 */
public class UnkillableSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.DEATH || event.getSource() != owner) {
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
        return "Unkillable";
    }
}
