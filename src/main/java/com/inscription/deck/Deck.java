package com.inscription.deck;

import com.inscription.model.Card;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Information hiding: the underlying list and shuffle logic are private.
 * Nobody outside this class can reorder the deck, remove an arbitrary card,
 * or see anything beyond the single front card - and even that requires
 * going through peekTop(), a deliberate, narrow exception for effects like
 * the Magnifying Glass item that need to preview a draw without taking it.
 */
public final class Deck {

    private final LinkedList<Card> cards;

    public Deck(List<Card> initialCards) {
        this.cards = new LinkedList<>(initialCards);
        shuffle();
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("Deck is empty");
        }
        return cards.removeFirst();
    }

    /**
     * Looks at the next card without drawing it - used by effects like the
     * Magnifying Glass item. Still can't see anything past the front card
     * or reorder the deck; this is a narrow, deliberate exception to the
     * seal, not a general "list everything" escape hatch.
     */
    public Card peekTop() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("Deck is empty");
        }
        return cards.getFirst();
    }

    /**
     * Adds a new card into the deck (e.g. a run event's reward) and
     * reshuffles, so its draw position isn't predictable. This only ever
     * adds - nothing outside Deck can still peek at, reorder, or remove an
     * arbitrary card; the seal on "what's coming next" stays intact.
     */
    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        cards.add(card);
        shuffle();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int remaining() {
        return cards.size();
    }

    private void shuffle() {
        Collections.shuffle(cards);
    }
}
