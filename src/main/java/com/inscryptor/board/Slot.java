package com.inscryptor.board;

import com.inscryptor.exception.SlotOccupiedException;
import com.inscryptor.model.Card;

public class Slot {

    private Card occupant;

    public boolean isEmpty() {
        return occupant == null;
    }

    public Card getOccupant() {
        return occupant;
    }

    public void place(Card card) {
        if (!isEmpty()) {
            throw new SlotOccupiedException("Slot is already occupied by " + occupant.getName());
        }
        occupant = card;
    }

    public void clear() {
        occupant = null;
    }
}
