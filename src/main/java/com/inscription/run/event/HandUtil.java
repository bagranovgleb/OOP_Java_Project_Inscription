package com.inscription.run.event;

import com.inscription.model.Card;
import com.inscription.model.DeckType;
import com.inscription.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Shared helper for events that operate on hand (Bone Altar, Campfire,
 * Mysterious Stones, Mycologists). Hand doesn't reliably have anything in
 * it between battles - whatever's left over depends entirely on how the
 * previous fight went - so these events pull the player's whole animal
 * deck into hand for the duration of the event, then return whatever's
 * left unused back afterward.
 * <p>
 * An earlier version of this only topped hand up to a small number (e.g.
 * 3) via ordinary one-at-a-time draws. That solved the "hand is often
 * empty" problem but created a new one: the player could only ever act on
 * whatever those few random draws happened to be, with no way to reach a
 * specific card they actually wanted to target (e.g. wanting to put Worthy
 * Sacrifice on a particular Black Cat, but only ever seeing a Wolf).
 * Pulling in the whole deck fixes that - the player sees and chooses from
 * everything they're carrying, not a random handful.
 * <p>
 * This still never lets anything outside Deck search it, reorder it, or
 * pull one specific card out by name - drawEntireDeckIntoHand() only ever
 * calls the same one-at-a-time drawFromDeck() every other draw in the game
 * uses, just repeated until empty, and returnUnusedDrawnCards() only ever
 * calls Deck's own existing, legitimate addCard() (already used elsewhere
 * for run event rewards) to put cards back. Deck's own sealed interface -
 * draw one from the top, add one back reshuffled, nothing else - is
 * completely unchanged; what changed is how much of it a hand-based event
 * chooses to draw through that interface before acting.
 */
final class HandUtil {

    private HandUtil() {
    }

    /**
     * Draws the player's entire animal deck into hand and returns exactly
     * which cards were drawn this way, so the caller can return whatever's
     * left of them afterward via returnUnusedDrawnCards(). Cards already in
     * hand before this call (leftover from an earlier battle) are left
     * alone and not tracked here - only what this call itself adds.
     */
    static List<Card> drawEntireDeckIntoHand(Player player) {
        List<Card> drawn = new ArrayList<>();
        while (true) {
            try {
                drawn.add(player.drawFromDeck(DeckType.ANIMAL));
            } catch (NoSuchElementException e) {
                break;
            }
        }
        return drawn;
    }

    /**
     * Returns whichever of the previously-drawn cards are still sitting in
     * hand - i.e. weren't consumed, sacrificed, fused, or otherwise removed
     * by the event itself - back into the deck, reshuffled. Anything the
     * event actually used is naturally excluded, since it's no longer in
     * hand to find.
     */
    static void returnUnusedDrawnCards(Player player, List<Card> drawn) {
        List<Card> hand = player.getHand();
        for (Card card : drawn) {
            if (hand.contains(card)) {
                player.removeFromHand(card);
                player.addToAnimalDeck(card);
            }
        }
    }
}
