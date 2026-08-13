package com.inscryptor.board;

import com.inscryptor.model.Card;

public class Board {

    private final Slot[] playerSlots;
    private final Slot[] opponentSlots;

    public Board(int slotsPerSide) {
        if (slotsPerSide <= 0) {
            throw new IllegalArgumentException("Must have at least one slot per side");
        }
        playerSlots = createSlots(slotsPerSide);
        opponentSlots = createSlots(slotsPerSide);
    }

    private Slot[] createSlots(int count) {
        Slot[] slots = new Slot[count];
        for (int i = 0; i < count; i++) {
            slots[i] = new Slot();
        }
        return slots;
    }

    public void placeCard(boolean isPlayerSide, int index, Card card) {
        Slot slot = getSlot(isPlayerSide, index);
        slot.place(card);
    }

    public Card removeCard(boolean isPlayerSide, int index) {
        Slot slot = getSlot(isPlayerSide, index);
        Card card = slot.getOccupant();
        slot.clear();
        return card;
    }

    /** Looks at whatever card occupies a slot without removing it. */
    public Card peekCard(boolean isPlayerSide, int index) {
        return getSlot(isPlayerSide, index).getOccupant();
    }

    public Slot[] getPlayerSlots() {
        return playerSlots.clone();
    }

    public Slot[] getOpponentSlots() {
        return opponentSlots.clone();
    }

    /** Clears any slot whose occupant has died, on either side of the board. */
    public void clearDeadCards() {
        clearDeadCards(playerSlots);
        clearDeadCards(opponentSlots);
    }

    private void clearDeadCards(Slot[] slots) {
        for (Slot slot : slots) {
            Card occupant = slot.getOccupant();
            if (occupant != null && !occupant.isAlive()) {
                slot.clear();
            }
        }
    }

    private Slot getSlot(boolean isPlayerSide, int index) {
        Slot[] slots = isPlayerSide ? playerSlots : opponentSlots;
        if (index < 0 || index >= slots.length) {
            throw new IndexOutOfBoundsException("Invalid slot index: " + index);
        }
        return slots[index];
    }
}
