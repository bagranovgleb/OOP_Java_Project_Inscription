package com.inscription.deck;

import com.inscription.model.CardType;

import java.util.List;

/**
 * Named deck presets. Kept separate from Deck itself (which only knows how
 * to shuffle and draw, not what should be in it) so a preset can change
 * without touching deck mechanics - useful once the run/path system needs to
 * hand out different starting decks depending on choices made along the way.
 */
public final class StarterDecks {

    private StarterDecks() {
    }

    /**
     * A deliberately minimal opening deck - just 3 cards - paired with the
     * Woodlands' opening event now being a Trader visit and 2 starting
     * Rabbit Pelts (see RunGame.main()), so the player's very first action
     * is spending those pelts to round out their hand before the first
     * fight, rather than starting with a full 8-card deck outright.
     */
    public static Deck beginnerAnimalDeck() {
        return new Deck(List.of(
            CardType.WOLF.create(),
            CardType.BULLFROG.create(),
            CardType.SKUNK.create()));
    }

    /** Pure sacrifice fodder - the squirrel deck barely needs a "beginner" version, but kept alongside for symmetry. */
    public static Deck beginnerSquirrelDeck() {
        List<com.inscription.model.Card> squirrels = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            squirrels.add(CardType.SQUIRREL.create());
        }
        return new Deck(squirrels);
    }
}
