package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.DeckType;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;
import com.inscription.player.Player;

import java.util.NoSuchElementException;

/**
 * When this card is played, immediately draws an extra card from the
 * player's animal deck. The reference sigil lets the player choose which
 * card to draw; doing that for real would mean exposing deck contents,
 * which breaks Deck's information hiding (nothing outside Deck can peek at
 * or reorder what's left). Simplified for now to an immediate bonus draw -
 * revisit once there's a UI to actually choose from.
 */
public class HoarderSigil implements Sigil {

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.PLACE || event.getSource() != owner) {
            return;
        }
        Player controllingPlayer = ownerOf(owner, context);
        if (controllingPlayer == null) {
            return;
        }
        try {
            controllingPlayer.drawFromDeck(DeckType.ANIMAL);
        } catch (NoSuchElementException emptyDeck) {
            // Nothing left to draw.
        }
    }

    @Override
    public String getName() {
        return "Hoarder";
    }
}
