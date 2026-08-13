package com.inscryptor;

import com.inscryptor.board.Board;
import com.inscryptor.deck.Deck;
import com.inscryptor.engine.GameEngine;
import com.inscryptor.exception.ConflictingModifierException;
import com.inscryptor.exception.InsufficientResourcesException;
import com.inscryptor.exception.SlotOccupiedException;
import com.inscryptor.model.Card;
import com.inscryptor.model.CardType;
import com.inscryptor.model.DeckType;
import com.inscryptor.model.ResourceType;
import com.inscryptor.modifier.LimitedSacrificesModifier;
import com.inscryptor.modifier.RestrictedDrawModifier;
import com.inscryptor.modifier.SigilTotemModifier;
import com.inscryptor.player.Player;

import java.util.List;

/**
 * Console driver that exercises the core game logic without any GUI, so you
 * can see every OOP piece working before wiring up JavaFX. Cards here come
 * from the CardType catalog (com.inscryptor.model.CardType) rather than
 * one-class-per-card - only cards with genuinely unique behavior (like a
 * boss overriding onAttack) get their own subclass.
 */
public class Main {

    public static void main(String[] args) {
        Board board = new Board(4);

        // Two decks per player: animal deck (real creatures, sigils) and a
        // squirrel-only deck (pure sacrifice fodder).
        Deck playerAnimalDeck = new Deck(List.of(CardType.WOLF.create(), CardType.WOLF.create()));
        Deck playerSquirrelDeck = new Deck(List.of(
            CardType.SQUIRREL.create(), CardType.SQUIRREL.create(), CardType.SQUIRREL.create()));
        Deck opponentAnimalDeck = new Deck(List.of(CardType.WOLF.create()));
        Deck opponentSquirrelDeck = new Deck(List.of(CardType.SQUIRREL.create(), CardType.SQUIRREL.create()));

        Player player = new Player("Player", playerAnimalDeck, playerSquirrelDeck);
        Player opponent = new Player("Opponent", opponentAnimalDeck, opponentSquirrelDeck);

        GameEngine engine = new GameEngine(board, player, opponent);

        System.out.println("=== Opening hand: 1 squirrel + 1 animal card ===");
        player.dealOpeningHand(1);
        System.out.println(player.getName() + "'s hand: " + player.getHand());

        System.out.println("\n=== Turn 2 draw choice: take an animal card ===");
        Card drawnAnimal = player.drawFromDeck(DeckType.ANIMAL);
        System.out.println("Drew: " + drawnAnimal + " | hand: " + player.getHand());

        System.out.println("\n=== Turn 3 draw choice: take another squirrel instead ===");
        Card drawnSquirrel = player.drawFromDeck(DeckType.SQUIRREL);
        System.out.println("Drew: " + drawnSquirrel + " | hand: " + player.getHand());

        System.out.println("\n=== Squirrel's identity sigil is hidden from the UI ===");
        System.out.println("All sigils (engine view): " +
            drawnSquirrel.getSigils().stream().map(s -> s.getName()).toList());
        System.out.println("Visible sigils (UI view): " +
            drawnSquirrel.getVisibleSigils().stream().map(s -> s.getName()).toList());
        System.out.println("hasSigil(\"Squirrel\")? " + drawnSquirrel.hasSigil("Squirrel"));

        System.out.println("\n=== Sacrifice for blood: must be on the board first ===");
        player.removeFromHand(drawnSquirrel);
        board.placeCard(true, 2, drawnSquirrel);
        engine.sacrificeFromBoard(true, 2);
        System.out.println("Blood after sacrifice: " + player.getBlood());

        System.out.println("\n=== Spend blood, catch overspend ===");
        player.spendResource(ResourceType.BLOOD, 1);
        System.out.println("Blood after spend: " + player.getBlood());
        try {
            player.spendResource(ResourceType.BLOOD, 5);
        } catch (InsufficientResourcesException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        System.out.println("\n=== Place cards on the board ===");
        // Found by tribe sigil, not by Java class - any Canine card would
        // match, not just this specific catalog entry.
        Card wolf = player.getHand().stream()
                .filter(c -> c.hasSigil("Canine"))
                .findFirst()
                .orElseThrow();
        player.removeFromHand(wolf);
        board.placeCard(true, 0, wolf);
        try {
            board.placeCard(true, 0, CardType.WOLF.create());
        } catch (SlotOccupiedException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        Card opponentPorcupine = CardType.PORCUPINE.create();
        board.placeCard(false, 0, opponentPorcupine);

        System.out.println("\n=== Sharp Quills: attacking it should hurt the attacker ===");
        System.out.println("Before: attacker=" + wolf + " defender=" + opponentPorcupine);
        engine.resolveAttack(wolf, opponentPorcupine);
        System.out.println("After:  attacker=" + wolf + " defender=" + opponentPorcupine);

        System.out.println("\n=== Diver: submerges after attacking, resurfaces at turn start ===");
        // Not tied to a specific catalog card yet - attach it ad hoc to
        // demonstrate the sigil on its own, composed onto an existing card.
        Card diver = CardType.SQUIRREL.create();
        diver.addSigil(new com.inscryptor.sigil.DiverSigil());
        board.placeCard(false, 1, diver);
        System.out.println("Can be targeted before it ever attacks? " + diver.canBeTargeted());
        diver.applySigils(
            new com.inscryptor.model.GameEvent(com.inscryptor.model.GameEventType.MOVE, diver, null), null);
        System.out.println("Can be targeted right after attacking?  " + diver.canBeTargeted());
        diver.applySigils(
            new com.inscryptor.model.GameEvent(com.inscryptor.model.GameEventType.TURN_START, diver, null), null);
        System.out.println("Can be targeted after its turn starts?  " + diver.canBeTargeted());

        System.out.println("\n=== Squirrel totem: buffs every Squirrel-sigil card on the board ===");
        Card totemSquirrel = CardType.SQUIRREL.create();
        board.placeCard(true, 1, totemSquirrel);
        System.out.println("Before totem: " + totemSquirrel);
        engine.addModifier(new SigilTotemModifier("Squirrel", 2, 0));
        engine.applyModifiers();
        System.out.println("After totem:  " + totemSquirrel);

        System.out.println("\n=== Conflicting ChallengeModifiers ===");
        engine.addModifier(new LimitedSacrificesModifier(1));
        System.out.println("Added: Limited Sacrifices");
        try {
            engine.addModifier(new RestrictedDrawModifier(1));
        } catch (ConflictingModifierException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
    }
}
