package com.inscryptor.sigil;

import com.inscryptor.engine.GameContext;
import com.inscryptor.model.Card;
import com.inscryptor.model.GameEvent;

/**
 * A special ability attached to a card. The engine and Card class never check
 * "is this a SharpQuills card?" - they just call apply() on whatever sigils are
 * present, so new sigils can be added without touching existing code.
 */
public interface Sigil {

    /** React to a game event (attack, death, turn start, ...). */
    void apply(GameEvent event, Card owner, GameContext context);

    String getName();

    /**
     * Whether this sigil currently prevents its owner from being targeted
     * (e.g. a submerged Diver). Default: never blocks targeting.
     */
    default boolean preventsTargeting(Card owner) {
        return false;
    }

    /**
     * Identity sigils (e.g. "Squirrel") mark what a card fundamentally is so
     * totem-style modifiers can check for them generically via
     * {@code Card.hasSigil(name)} instead of an instanceof chain - but they carry
     * no active effect and shouldn't clutter the card's UI. Default: shown normally.
     */
    default boolean isHidden() {
        return false;
    }
}
