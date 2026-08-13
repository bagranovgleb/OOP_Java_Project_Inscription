package com.inscryptor.player;

import com.inscryptor.deck.Deck;
import com.inscryptor.exception.InsufficientResourcesException;
import com.inscryptor.exception.InvalidSacrificeException;
import com.inscryptor.model.Card;
import com.inscryptor.model.DeckType;
import com.inscryptor.model.ResourceType;

import java.util.ArrayList;
import java.util.List;

public class Player {

    public static final int STARTING_LIFE = 5;

    private final String name;
    private final Deck animalDeck;
    private final Deck squirrelDeck;
    private final List<Card> hand = new ArrayList<>();
    private int blood;
    private int bones;
    private int life = STARTING_LIFE;

    public Player(String name, Deck animalDeck, Deck squirrelDeck) {
        this.name = name;
        this.animalDeck = animalDeck;
        this.squirrelDeck = squirrelDeck;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return List.copyOf(hand);
    }

    public int getBlood() {
        return blood;
    }

    public int getBones() {
        return bones;
    }

    public int getLife() {
        return life;
    }

    public boolean isDefeated() {
        return life <= 0;
    }

    /** Damage that lands directly on the player - an unblocked lane attack. */
    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        life = Math.max(0, life - amount);
    }

    /**
     * Deals the opening hand: one squirrel plus a number of animal cards.
     * Mirrors the game's start-of-run setup, before the per-turn draw choice
     * kicks in.
     */
    public void dealOpeningHand(int animalCardCount) {
        drawFromDeck(DeckType.SQUIRREL);
        for (int i = 0; i < animalCardCount; i++) {
            drawFromDeck(DeckType.ANIMAL);
        }
    }

    /**
     * Draws one card from the chosen deck for the turn's draw phase. The
     * player picks exactly one of the two decks each turn - a fresh animal
     * card, or another squirrel to keep as sacrifice fuel - never both.
     * Player never touches either deck's internals; drawing goes through
     * Deck.drawCard() only.
     */
    public Card drawFromDeck(DeckType choice) {
        Deck deck = (choice == DeckType.ANIMAL) ? animalDeck : squirrelDeck;
        Card card = deck.drawCard();
        hand.add(card);
        return card;
    }

    /** Whether there's anything left to draw from either deck. */
    public boolean canDraw() {
        return !animalDeck.isEmpty() || !squirrelDeck.isEmpty();
    }

    /**
     * Sacrifices a card for one blood and one bone. The card must already
     * have been removed from wherever it was (the board) before this is
     * called - Player doesn't know about the board, so it can't verify that
     * itself.
     */
    /**
     * Sacrifices a card for blood and one bone. The card must already have
     * been removed from wherever it was (the board) before this is called -
     * Player doesn't know about the board, so it can't verify that itself.
     * A card with Many Lives survives the sacrifice instead of perishing.
     * A card with Worthy Sacrifice is worth 3 blood instead of the usual 1.
     */
    public void sacrificeCard(Card card) {
        if (card == null) {
            throw new InvalidSacrificeException("No card to sacrifice");
        }
        if (!card.hasSigil("Many Lives")) {
            card.sacrifice();
        }
        blood += card.hasSigil("Worthy Sacrifice") ? 3 : 1;
        bones++;
    }

    public void gainBones(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        bones += amount;
    }

    public void spendResource(ResourceType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        switch (type) {
            case BLOOD -> {
                if (blood < amount) {
                    throw new InsufficientResourcesException(
                        "Not enough blood: have " + blood + ", need " + amount);
                }
                blood -= amount;
            }
            case BONES -> {
                if (bones < amount) {
                    throw new InsufficientResourcesException(
                        "Not enough bones: have " + bones + ", need " + amount);
                }
                bones -= amount;
            }
        }
    }

    /** Removes a card from hand so it can be placed on the board. */
    public void removeFromHand(Card card) {
        if (!hand.contains(card)) {
            throw new IllegalStateException("Card is not in hand: " + card.getName());
        }
        hand.remove(card);
    }
}
