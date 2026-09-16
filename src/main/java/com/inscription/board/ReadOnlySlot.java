package com.inscription.board;

import com.inscription.model.Card;

/**
 * Read-only view of a board slot - lets anyone check what's there without
 * being able to place or clear it. The only way to actually mutate a slot
 * is through Board's own placeCard()/removeCard()/clearDeadCards(), which
 * enforce the real rules (e.g. can't place into an occupied slot). Slot
 * still implements this and keeps its own place()/clear() methods for
 * Board's internal use - only code holding this narrower interface type
 * loses the ability to call them.
 */
public interface ReadOnlySlot {

    boolean isEmpty();

    Card getOccupant();
}
