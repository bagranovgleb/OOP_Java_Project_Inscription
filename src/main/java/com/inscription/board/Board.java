package com.inscription.board;

import com.inscription.model.Card;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final Slot[] playerSlots;
    private final Slot[] opponentSlots;

    /**
     * Every player-side card that has died on this board, captured at the
     * exact moment clearDeadCards() drops it from its slot - that's the
     * only point where the dying Card object is still reachable at all,
     * since clearing a slot just nulls out the reference otherwise, with
     * nothing else in the program still holding onto it. Battle reclaims
     * this list back into the player's hand at the end of the fight
     * (alongside survivors), so a card that died still eventually finds
     * its way back into the deck rather than being permanently lost.
     * Opponent-side deaths aren't tracked here at all - the opponent
     * Player object is thrown away after the battle regardless, so there
     * would be nothing to reclaim it into.
     */
    private final List<Card> playerGraveyard = new ArrayList<>();

    /**
     * The opponent's second rank - cards waiting for a front-row slot to
     * open up. Only the opponent has one: the player's side is always a
     * single row. A reserve card auto-promotes into the matching front
     * lane the moment that lane empties (see promoteAllPossible()), and
     * overkill damage that kills a front-row occupant pierces through into
     * whatever's waiting behind it in the same lane.
     */
    private final Slot[] opponentReserveSlots;

    /**
     * The opponent can only ever place a NEW card into reserve, never
     * directly into the front row (see SimpleOpponentAI) - reserve cards
     * promote to front at the start of the opponent's own next turn (see
     * GameEngine.promoteReserveCards(), called from Battle.opponentTurn()),
     * giving every opponent card exactly one full turn of "preparation"
     * before it can attack. Since reserve is the only way in, it needs to
     * match the front row's full capacity - otherwise lanes 2 and 3 would
     * be permanently unreachable for the opponent.
     */
    private static final int MAX_OPPONENT_RESERVE_SIZE = 4;

    public Board(int slotsPerSide) {
        if (slotsPerSide <= 0) {
            throw new IllegalArgumentException("Must have at least one slot per side");
        }
        playerSlots = createSlots(slotsPerSide);
        opponentSlots = createSlots(slotsPerSide);
        opponentReserveSlots = createSlots(Math.min(slotsPerSide, MAX_OPPONENT_RESERVE_SIZE));
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

    public ReadOnlySlot[] getPlayerSlots() {
        return playerSlots.clone();
    }

    public ReadOnlySlot[] getOpponentSlots() {
        return opponentSlots.clone();
    }

    public ReadOnlySlot[] getOpponentReserveSlots() {
        return opponentReserveSlots.clone();
    }

    public void placeReserveCard(int lane, Card card) {
        getReserveSlot(lane).place(card);
    }

    public Card removeReserveCard(int lane) {
        Slot slot = getReserveSlot(lane);
        Card card = slot.getOccupant();
        slot.clear();
        return card;
    }

    /** Returns null both for an empty reserve slot and for a lane that has no reserve capacity at all - callers checking "is anything waiting here" don't need to care which. */
    public Card peekReserveCard(int lane) {
        if (lane < 0 || lane >= opponentReserveSlots.length) {
            return null;
        }
        return opponentReserveSlots[lane].getOccupant();
    }

    /**
     * If the opponent's front lane is empty and a reserve card is waiting
     * behind it, moves that reserve card up. Returns whether a promotion
     * happened. Lanes beyond the reserve's (smaller) capacity simply never
     * have anything to promote - this returns false rather than throwing,
     * since callers loop over the front row's full length.
     */
    public boolean promoteReserveIfPossible(int lane) {
        if (lane < 0 || lane >= opponentReserveSlots.length) {
            return false;
        }
        if (opponentSlots[lane].getOccupant() == null && opponentReserveSlots[lane].getOccupant() != null) {
            Card promoted = removeReserveCard(lane);
            opponentSlots[lane].place(promoted);
            return true;
        }
        return false;
    }

    /** Promotes every lane where the front is empty and reserve has something waiting. */
    public void promoteAllPossible() {
        for (int lane = 0; lane < opponentSlots.length; lane++) {
            promoteReserveIfPossible(lane);
        }
    }

    /** Clears any slot whose occupant has died, across the player's row, the opponent's front row, and the opponent's reserve row - capturing player-side deaths into the graveyard first. */
    public void clearDeadCards() {
        clearDeadCards(playerSlots, true);
        clearDeadCards(opponentSlots, false);
        clearDeadCards(opponentReserveSlots, false);
    }

    private void clearDeadCards(Slot[] slots, boolean isPlayerSide) {
        for (Slot slot : slots) {
            Card occupant = slot.getOccupant();
            if (occupant != null && !occupant.isAlive()) {
                if (isPlayerSide) {
                    playerGraveyard.add(occupant);
                }
                slot.clear();
            }
        }
    }

    /** Every player-side card that has died on this board so far - see the field's own doc for why this exists and how Battle uses it. */
    public List<Card> getPlayerGraveyard() {
        return List.copyOf(playerGraveyard);
    }

    private Slot getSlot(boolean isPlayerSide, int index) {
        Slot[] slots = isPlayerSide ? playerSlots : opponentSlots;
        if (index < 0 || index >= slots.length) {
            throw new IndexOutOfBoundsException("Invalid slot index: " + index);
        }
        return slots[index];
    }

    private Slot getReserveSlot(int index) {
        if (index < 0 || index >= opponentReserveSlots.length) {
            throw new IndexOutOfBoundsException("Invalid reserve slot index: " + index);
        }
        return opponentReserveSlots[index];
    }
}
