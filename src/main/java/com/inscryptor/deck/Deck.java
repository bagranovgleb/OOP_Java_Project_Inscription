package com.inscryptor.deck;

import com.inscryptor.model.Card;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Information hiding: the underlying list and shuffle logic are private.
 * Nobody outside this class - not even Player - can reorder the deck or peek
 * at what's coming next. The only way in or out is drawCard().
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
