package com.inscription.cli;

import com.inscription.ai.OpponentAI;
import com.inscription.ai.SimpleOpponentAI;
import com.inscription.board.Board;
import com.inscription.board.Slot;
import com.inscription.deck.Deck;
import com.inscription.engine.GameEngine;
import com.inscription.engine.HealthScale;
import com.inscription.exception.InsufficientResourcesException;
import com.inscription.exception.InvalidSacrificeException;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.DeckType;
import com.inscription.player.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Text-based, playable entry point. Only reads input and calls the engine/
 * player/board API built for this project - no rule logic lives here that
 * doesn't already exist as a method call elsewhere. That boundary is what
 * lets this loop be swapped for a JavaFX controller later without touching
 * the game logic itself.
 */
public class ConsoleGame {

    private final Scanner scanner = new Scanner(System.in);
    private final Board board = new Board(4);
    private final Player player;
    private final Player opponent;
    private final GameEngine engine;
    private final OpponentAI opponentAI = new SimpleOpponentAI();

    private boolean hasDrawnThisTurn;
    private boolean quit;

    public static void main(String[] args) {
        new ConsoleGame().run();
    }

    public ConsoleGame() {
        Deck playerAnimalDeck = new Deck(List.of(
            CardType.WOLF.create(), CardType.WOLF.create(),
            CardType.PORCUPINE.create(), CardType.WOLF.create()));
        Deck playerSquirrelDeck = new Deck(List.of(
            CardType.SQUIRREL.create(), CardType.SQUIRREL.create(),
            CardType.SQUIRREL.create(), CardType.SQUIRREL.create()));
        Deck opponentAnimalDeck = new Deck(List.of(
            CardType.WOLF.create(), CardType.WOLF.create(), CardType.PORCUPINE.create()));
        Deck opponentSquirrelDeck = new Deck(List.of(
            CardType.SQUIRREL.create(), CardType.SQUIRREL.create(), CardType.SQUIRREL.create()));

        player = new Player("You", playerAnimalDeck, playerSquirrelDeck);
        opponent = new Player("Opponent", opponentAnimalDeck, opponentSquirrelDeck);
        engine = new GameEngine(board, player, opponent);

        // Enemies start with a pre-built board presence, not just a hand.
        board.placeCard(false, 0, CardType.PORCUPINE.create());
    }

    public void run() {
        System.out.println("=== Inscription: card battler prototype ===");
        printHelp();

        player.dealOpeningHand(2);
        opponent.dealOpeningHand(2);

        HealthScale scale = engine.getHealthScale();
        while (!quit && !scale.isPlayerVictorious() && !scale.isOpponentVictorious()) {
            playerTurn();
            if (quit || scale.isPlayerVictorious()) {
                break;
            }
            opponentTurn();
        }

        if (scale.isOpponentVictorious()) {
            System.out.println("\nYou were defeated. Game over.");
        } else if (scale.isPlayerVictorious()) {
            System.out.println("\nYou win! The opponent was defeated.");
        } else {
            System.out.println("\nGoodbye.");
        }
        printGoldenTeeth(scale);
        scanner.close();
    }

    private void printGoldenTeeth(HealthScale scale) {
        System.out.println("\nGolden teeth earned (bonus for overkill damage):");
        System.out.println("  You:      " + scale.getPlayerGoldenTeeth());
        System.out.println("  Opponent: " + scale.getOpponentGoldenTeeth());
    }

    private void playerTurn() {
        engine.startTurn(true);
        hasDrawnThisTurn = false;
        System.out.println("\n----- Your turn -----");
        printStatus();

        requireDraw();

        boolean turnOver = false;
        while (!turnOver && !quit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            String command = tokens[0].toLowerCase();

            switch (command) {
                case "help" -> printHelp();
                case "hand" -> printHand();
                case "board" -> printBoard();
                case "status" -> printStatus();
                case "draw" -> handleDraw(tokens);
                case "place" -> handlePlace(tokens);
                case "sacrifice" -> handleSacrifice(tokens);
                case "bell" -> {
                    ringBellForPlayer();
                    turnOver = true;
                }
                case "quit" -> {
                    quit = true;
                    turnOver = true;
                }
                default -> System.out.println("Unknown command. Type 'help' for the list.");
            }
        }
    }

    /**
     * Drawing is mandatory at the start of every turn - nothing else (place,
     * sacrifice, ring the bell) is allowed until it happens. Read-only
     * commands still work so the player can check their hand/board first.
     */
    private void requireDraw() {
        if (!player.canDraw()) {
            System.out.println("Both decks are empty - no draw this turn.");
            return;
        }
        System.out.println("You must draw before doing anything else this turn.");
        while (!hasDrawnThisTurn && !quit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            String command = tokens[0].toLowerCase();

            switch (command) {
                case "draw" -> handleDraw(tokens);
                case "help" -> printHelp();
                case "hand" -> printHand();
                case "board" -> printBoard();
                case "status" -> printStatus();
                case "quit" -> quit = true;
                default -> System.out.println("You must draw first. Usage: draw <animal|squirrel>");
            }
        }
    }

    private void opponentTurn() {
        engine.startTurn(false);
        System.out.println("\n----- Opponent's turn -----");
        opponentAI.takeTurn(engine, board, opponent, player);
        System.out.println("Opponent acted. " + describeScale());
        printBoard();
    }

    private void handleDraw(String[] tokens) {
        if (hasDrawnThisTurn) {
            System.out.println("You've already drawn this turn.");
            return;
        }
        if (tokens.length < 2) {
            System.out.println("Usage: draw <animal|squirrel>");
            return;
        }
        DeckType type = switch (tokens[1].toLowerCase()) {
            case "animal" -> DeckType.ANIMAL;
            case "squirrel" -> DeckType.SQUIRREL;
            default -> null;
        };
        if (type == null) {
            System.out.println("Usage: draw <animal|squirrel>");
            return;
        }
        try {
            Card drawn = player.drawFromDeck(type);
            hasDrawnThisTurn = true;
            System.out.println("Drew: " + drawn);
        } catch (NoSuchElementException e) {
            System.out.println("That deck is empty.");
        }
    }

    private void handlePlace(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println("Usage: place <hand index> <slot index>");
            return;
        }
        Integer handIndex = parseInt(tokens[1]);
        Integer slotIndex = parseInt(tokens[2]);
        if (handIndex == null || slotIndex == null) {
            System.out.println("Usage: place <hand index> <slot index>");
            return;
        }
        List<Card> hand = player.getHand();
        if (handIndex < 0 || handIndex >= hand.size()) {
            System.out.println("No card at hand index " + handIndex + ". Type 'hand' to see your hand.");
            return;
        }
        Card card = hand.get(handIndex);

        // Check the slot is free before spending anything, so a failed
        // placement never costs the player resources.
        Slot[] playerSlots = board.getPlayerSlots();
        if (slotIndex < 0 || slotIndex >= playerSlots.length) {
            System.out.println("Invalid slot index: " + slotIndex);
            return;
        }
        if (!playerSlots[slotIndex].isEmpty()) {
            System.out.println("Slot " + slotIndex + " is already occupied by "
                + playerSlots[slotIndex].getOccupant().getName() + ".");
            return;
        }

        if (card.getCost() > 0) {
            try {
                player.spendResource(card.getCostType(), card.getCost());
            } catch (InsufficientResourcesException e) {
                System.out.println(e.getMessage());
                return;
            }
        }

        engine.placeCard(true, slotIndex, card);
        player.removeFromHand(card);
        System.out.println("Placed " + card.getName() + " in slot " + slotIndex + ".");
    }

    private void handleSacrifice(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: sacrifice <lane index>");
            return;
        }
        Integer lane = parseInt(tokens[1]);
        if (lane == null) {
            System.out.println("Usage: sacrifice <lane index>");
            return;
        }
        try {
            engine.sacrificeFromBoard(true, lane);
            System.out.println("Sacrificed the creature in lane " + lane + ". Blood: " + player.getBlood());
        } catch (InvalidSacrificeException | IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
    }

    private void ringBellForPlayer() {
        System.out.println("*ring* Combat begins - your creatures attack left to right.");
        engine.ringBell(true);
        printBoard();
        System.out.println(describeScale());
    }

    private Integer parseInt(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
              hand                       show your hand (with indices)
              board                      show the board
              status                     show the scale / blood / bones
              draw animal|squirrel       mandatory once per turn, before anything else
              place <hand#> <slot#>      play a card from hand onto a board slot
                                         (pays its {cost: N blood/bones} if it has one)
              sacrifice <lane#>          sacrifice a creature on your board for blood
              bell                       ring the bell: your creatures attack left to
                                         right (hitting face if a lane is undefended),
                                         then it's the opponent's turn
              quit                       leave the game
            """);
    }

    private void printStatus() {
        System.out.println(describeScale() + " | Blood: " + player.getBlood() + " | Bones: " + player.getBones());
        printHand();
        printBoard();
    }

    /**
     * One number, 0-10, centered at 5 - not two separate life totals. Higher
     * favors the player (10 = opponent defeated), lower favors the opponent
     * (0 = player defeated).
     */
    private String describeScale() {
        int value = engine.getHealthScale().getValue();
        return "Scale: " + value + "/10 (you win at 10, lose at 0)";
    }

    private void printHand() {
        List<Card> hand = player.getHand();
        StringBuilder sb = new StringBuilder("Hand: ");
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            sb.append("[").append(i).append("] ").append(card.describe(engine.getDisplayAttack(card, true)));
            if (i < hand.size() - 1) {
                sb.append("  ");
            }
        }
        System.out.println(hand.isEmpty() ? "Hand: (empty)" : sb);
    }

    private void printBoard() {
        Slot[] playerSlots = board.getPlayerSlots();
        Slot[] opponentSlots = board.getOpponentSlots();
        System.out.println("Board (lane: you | opponent):");
        for (int i = 0; i < playerSlots.length; i++) {
            String mine = describeSlot(playerSlots[i]);
            String theirs = describeSlot(opponentSlots[i]);
            System.out.printf("  [%d] %-20s | %-20s%n", i, mine, theirs);
        }
    }

    private String describeSlot(Slot slot) {
        Card occupant = slot.getOccupant();
        if (occupant == null) {
            return "(empty)";
        }
        return occupant.describe(engine.getDisplayAttack(occupant, true));
    }
}
