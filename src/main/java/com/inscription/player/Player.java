package com.inscription.player;

import com.inscription.deck.Deck;
import com.inscription.exception.InsufficientResourcesException;
import com.inscription.exception.InvalidSacrificeException;
import com.inscription.item.Item;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.DeckType;
import com.inscription.model.ResourceType;
import com.inscription.pelt.PeltKind;
import com.inscription.totem.ActiveTotem;
import com.inscription.totem.TotemBody;
import com.inscription.totem.TotemHead;
import com.inscription.totem.TotemPiece;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Player {

    /** "Consumable Items" are capped at 3 held at once, per the reference sheet. */
    public static final int MAX_ITEMS = 3;

    /** Total totem pieces (heads + bodies combined) capped at 6. */
    public static final int MAX_TOTEM_PIECES = 6;

    private final String name;
    private final Deck animalDeck;
    private final Deck squirrelDeck;
    private final List<Card> hand = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<TotemPiece> totemPieces = new ArrayList<>();
    private ActiveTotem activeTotem;
    private int bones;

    /** Golden teeth are earned in battle (HealthScale's overflow damage tracking) and banked here permanently once a battle ends. */
    private int teeth;

    private final java.util.Map<PeltKind, Integer> pelts = new java.util.EnumMap<>(PeltKind.class);
    private boolean receivedFreeRabbitPelt;

    /** Lives for the whole run - lose one on a genuine battle defeat (not on quitting); the run ends at 0. Bosses cap this to 1 regardless of how many the player arrived with. */
    public static final int STARTING_CANDLES = 2;
    private int candles = STARTING_CANDLES;

    /** Accumulated from Bone Altar visits (Minor Boon +1, Boon +4) - granted at the start of every battle from then on. */
    private int bonusStartingBones;

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

    public int getBones() {
        return bones;
    }

    /** How many cards remain in the animal deck - used by RunGame to size an opponent's deck to match, so a growing player deck doesn't leave the opponent structurally out-drawn (or vice versa). */
    public int getAnimalDeckSize() {
        return animalDeck.remaining();
    }

    public int getTeeth() {
        return teeth;
    }

    public void gainTeeth(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        teeth += amount;
    }

    /** Spends teeth if there are enough. Returns whether the spend succeeded. */
    public boolean spendTeeth(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (teeth < amount) {
            return false;
        }
        teeth -= amount;
        return true;
    }

    public int getPeltCount(PeltKind kind) {
        return pelts.getOrDefault(kind, 0);
    }

    public void gainPelt(PeltKind kind) {
        pelts.merge(kind, 1, Integer::sum);
    }

    /** Spends one pelt of the given kind if there is one. Returns whether the spend succeeded. */
    public boolean spendPelt(PeltKind kind) {
        int current = getPeltCount(kind);
        if (current <= 0) {
            return false;
        }
        pelts.put(kind, current - 1);
        return true;
    }

    public boolean hasReceivedFreeRabbitPelt() {
        return receivedFreeRabbitPelt;
    }

    public void markFreeRabbitPeltReceived() {
        receivedFreeRabbitPelt = true;
    }

    public int getCandles() {
        return candles;
    }

    /** Puts out one candle (floored at 0) - call this on a genuine battle defeat, never on quitting. */
    public void loseCandle() {
        candles = Math.max(0, candles - 1);
    }

    /** Directly sets the candle count - used by boss encounters to cap the player to exactly 1, even if they arrived with more. */
    public void setCandles(int candles) {
        if (candles < 0) {
            throw new IllegalArgumentException("Candles cannot be negative");
        }
        this.candles = candles;
    }

    public boolean isOutOfCandles() {
        return candles <= 0;
    }

    /**
     * Deals the opening hand: one squirrel plus a number of animal cards.
     * Mirrors the game's start-of-run setup, before the per-turn draw choice
     * kicks in.
     */
    public void dealOpeningHand(int animalCardCount) {
        try {
            drawFromDeck(DeckType.SQUIRREL);
        } catch (NoSuchElementException e) {
            // Some players (e.g. certain opponents) may have no squirrels at all - that's fine, just skip it.
        }
        for (int i = 0; i < animalCardCount; i++) {
            try {
                drawFromDeck(DeckType.ANIMAL);
            } catch (NoSuchElementException e) {
                break; // deck exhausted (a genuine possibility now that deck sizes can be very small) - deal as many as are actually available
            }
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

    /**
     * Adds a card straight into the animal deck - used by run events that
     * reward a new card (Card Choice, Deck Trial, ...). Goes into the deck,
     * not the hand, so it's drawn normally in a future battle rather than
     * being available immediately.
     */
    public void addToAnimalDeck(Card card) {
        animalDeck.addCard(card);
    }

    /** Symmetric with addToAnimalDeck - used when reshuffling a Squirrel card in hand back into its own deck. */
    public void addToSquirrelDeck(Card card) {
        squirrelDeck.addCard(card);
    }

    /**
     * Squirrels are meant to feel like an effectively unlimited supply of
     * free fodder, not a resource that can genuinely run dry mid-fight -
     * tops the squirrel deck back up to a full 30 by adding fresh Squirrel
     * cards, called right before each battle starts. Does nothing if it's
     * already at or above 30 (e.g. from squirrels reshuffled back in after
     * a previous fight).
     */
    public void topUpSquirrelDeck() {
        while (squirrelDeck.remaining() < 30) {
            squirrelDeck.addCard(CardType.SQUIRREL.create());
        }
    }

    /**
     * Reshuffles every card currently in hand back into its matching deck -
     * Squirrels back into the squirrel deck, everything else back into the
     * animal deck - clearing hand entirely. Called after each event or
     * fight resolves, so a deck that's shrunk from repeated draws (Deck
     * Trial's permanent 3-card draw, a battle's opening hand and turn
     * draws, and so on) gets topped back up toward its original size from
     * cards the player still genuinely owns, rather than the deck being
     * able to run dry over a long run. Cards actually lost along the way
     * (sacrificed, destroyed, permanently removed by something like Bone
     * Altar) were already gone from hand before this runs, so they're
     * correctly not restored - only what the player is still holding comes
     * back.
     */
    public void reshuffleHandIntoDecks() {
        List<Card> handCopy = new ArrayList<>(hand);
        for (Card card : handCopy) {
            hand.remove(card);
            if (card.hasSigil("Conjured")) {
                continue; // "in a bottle" item cards vanish rather than joining either deck
            }
            if (card.hasSigil("Squirrel")) {
                addToSquirrelDeck(card);
            } else {
                addToAnimalDeck(card);
            }
        }
    }

    /** Looks at the next card that would be drawn from the animal deck, without drawing it - used by the Magnifying Glass item. */
    public Card peekAnimalDeck() {
        return animalDeck.peekTop();
    }

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public boolean hasRoomForItem() {
        return items.size() < MAX_ITEMS;
    }

    /** Adds an item if there's room. Returns whether it was actually added. */
    public boolean addItem(Item item) {
        if (!hasRoomForItem()) {
            return false;
        }
        items.add(item);
        return true;
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public List<TotemPiece> getTotemPieces() {
        return List.copyOf(totemPieces);
    }

    public boolean hasRoomForTotemPiece() {
        return totemPieces.size() < MAX_TOTEM_PIECES;
    }

    public void addTotemPiece(TotemPiece piece) {
        totemPieces.add(piece);
    }

    public boolean hasHeadForTribe(String tribe) {
        return totemPieces.stream()
            .filter(p -> p instanceof TotemHead)
            .map(p -> (TotemHead) p)
            .anyMatch(h -> h.getTribe().equals(tribe));
    }

    public List<TotemHead> getHeadsInInventory() {
        return totemPieces.stream()
            .filter(p -> p instanceof TotemHead)
            .map(p -> (TotemHead) p)
            .toList();
    }

    public List<TotemBody> getBodiesInInventory() {
        return totemPieces.stream()
            .filter(p -> p instanceof TotemBody)
            .map(p -> (TotemBody) p)
            .toList();
    }

    public ActiveTotem getActiveTotem() {
        return activeTotem;
    }

    /** Neither piece is removed from inventory - only the reference to "which combination is active" changes. */
    public void setActiveTotem(ActiveTotem totem) {
        this.activeTotem = totem;
    }

    /**
     * Adds a card straight to hand, bypassing the deck entirely - used by
     * sigils that conjure a card out of thin air (e.g. Unkillable's copy on
     * death) rather than drawing one.
     */
    public void addToHand(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        hand.add(card);
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
     * Sacrifices a card for one bone. The card must already have been
     * removed from wherever it was (the board) before this is called -
     * Player doesn't know about the board, so it can't verify that itself.
     * A card with Many Lives survives the sacrifice instead of perishing.
     * <p>
     * This does NOT track Blood at all - Blood is an original game
     * mechanic that never banks. It only ever exists for the instant
     * between sacrificing enough creatures and spending it on the specific
     * card being placed in that same action (see Battle's guided
     * sacrifice-then-place flow, which computes and uses Blood amounts
     * itself, including a card's Worthy Sacrifice bonus, entirely outside
     * of Player). Sacrificing standalone - not as part of placing a
     * specific card - still grants the Bone here, but any Blood that same
     * sacrifice would have produced is simply never captured at all.
     */
    public void sacrificeCard(Card card) {
        if (card == null) {
            throw new InvalidSacrificeException("No card to sacrifice");
        }
        if (!card.hasSigil("Many Lives")) {
            card.sacrifice();
        }
        bones++;
    }

    public void gainBones(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        bones += amount;
    }

    /**
     * Bones are a per-battle resource, not a permanent stockpile - called at
     * the start of every new battle (before any bonus starting bones from a
     * Bone Altar visit are granted), so nothing carries over from whatever
     * was left unspent in the previous fight.
     */
    public void resetBones() {
        bones = 0;
    }

    public int getBonusStartingBones() {
        return bonusStartingBones;
    }

    /** Stacks - a second Bone Altar visit adds its own bonus on top of any earlier one. */
    public void gainBonusStartingBones(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        bonusStartingBones += amount;
    }

    /** Spends Bones from the persistent pool. Blood is never spent through here - see the note on sacrificeCard(). */
    public void spendResource(ResourceType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (type != ResourceType.BONES) {
            throw new IllegalArgumentException(
                "spendResource only handles Bones - Blood never banks, so it's spent directly in Battle's sacrifice-then-place flow, not through here");
        }
        if (bones < amount) {
            throw new InsufficientResourcesException(
                "Not enough bones: have " + bones + ", need " + amount);
        }
        bones -= amount;
    }

    /** Removes a card from hand so it can be placed on the board. */
    public void removeFromHand(Card card) {
        if (!hand.contains(card)) {
            throw new IllegalStateException("Card is not in hand: " + card.getName());
        }
        hand.remove(card);
    }
}
