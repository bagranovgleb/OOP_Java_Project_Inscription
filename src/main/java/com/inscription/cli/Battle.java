package com.inscription.cli;

import com.inscription.ai.OpponentAI;
import com.inscription.ai.SimpleOpponentAI;
import com.inscription.board.Board;
import com.inscription.board.ReadOnlySlot;
import com.inscription.engine.GameEngine;
import com.inscription.engine.HealthScale;
import com.inscription.exception.InsufficientResourcesException;
import com.inscription.item.Item;
import com.inscription.item.ItemContext;
import com.inscription.model.Card;
import com.inscription.model.CardType;
import com.inscription.model.DeckType;
import com.inscription.model.ResourceType;
import com.inscription.player.Player;
import com.inscription.ui.BoardSnapshot;
import com.inscription.ui.ConsoleGameUI;
import com.inscription.ui.GameUI;
import com.inscription.util.GameRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A single fight - everything ConsoleGame used to do directly, extracted so
 * the same Player (and its carried-over deck) can fight more than one battle
 * along a run's path, each with a fresh board and health scale. Health never
 * carries over between battles (a new HealthScale is built fresh here every
 * time); the deck and hand do, since they live on the Player object the
 * caller passes in and keeps using afterward.
 * <p>
 * All display output and the two genuinely interactive prompts (accepting a
 * surrender, marking creatures to sacrifice) go through GameUI, so a future
 * JavaFX implementation could show its own board/hand widgets and dialogs
 * without this class knowing the difference. The main command loop's own
 * "read a line, tokenize it, switch on the command word" structure is left
 * as direct Scanner reads deliberately - that's inherently console-specific
 * infrastructure a GUI would replace entirely with button-click handlers
 * calling straight into the same handleX() methods, not something to
 * abstract behind an interface.
 */
public class Battle {

    private final Scanner scanner;
    private final GameUI ui;
    private final Board board = new Board(4);
    private final Player player;
    private final Player opponent;
    private final GameEngine engine;
    private final OpponentAI opponentAI = new SimpleOpponentAI();

    private boolean hasDrawnThisTurn;
    private boolean quit;

    public Battle(Player player, Player opponent, Scanner scanner) {
        this(player, opponent, scanner, false);
    }

    /** Same as the 3-arg version, but aware of whether this is a Snowline level - affects which obstacle map cards get chosen (Grand Fir becomes Snowy Fir). */
    public Battle(Player player, Player opponent, Scanner scanner, boolean isSnowLevel) {
        this(player, opponent, scanner, new ConsoleGameUI(scanner), isSnowLevel);
    }

    /** For a GUI context, which has no real Scanner to speak of - see the class-level note on how combat then works without one. */
    public Battle(Player player, Player opponent, GameUI ui) {
        this(player, opponent, ui, false);
    }

    /** Same as the 3-arg version, but aware of whether this is a Snowline level. */
    public Battle(Player player, Player opponent, GameUI ui, boolean isSnowLevel) {
        this(player, opponent, null, ui, isSnowLevel);
    }

    private Battle(Player player, Player opponent, Scanner scanner, GameUI ui, boolean isSnowLevel) {
        this.player = player;
        this.opponent = opponent;
        this.scanner = scanner;
        this.ui = ui;
        engine = new GameEngine(board, player, opponent);

        // Enemies start with a pre-built board presence, not just a hand - one of a
        // small set of pre-defined "obstacle maps" laying out which lanes (if any)
        // start occupied by terrain rather than a real creature. Grand Fir becomes
        // Snowy Fir specifically on a Snowline level, to match the theme.
        CardType firType = isSnowLevel ? CardType.SNOWY_FIR : CardType.GRAND_FIR;
        List<List<CardType>> obstacleMaps = List.of(
            Arrays.asList(null, null, null, null),
            Arrays.asList(null, CardType.STUMP, CardType.STUMP, null),
            Arrays.asList(null, firType, null, firType),
            Arrays.asList(CardType.STUMP, null, null, firType));
        List<CardType> chosenMap = obstacleMaps.get(GameRandom.create().nextInt(obstacleMaps.size()));
        for (int lane = 0; lane < chosenMap.size(); lane++) {
            CardType obstacle = chosenMap.get(lane);
            if (obstacle != null) {
                board.placeCard(false, lane, obstacle.create());
            }
        }

        // Bones are a per-battle resource, not something to stockpile across
        // an entire run - reset first, then grant any recurring Bone Altar
        // bonus on top of that clean 0, checked symmetrically for both
        // sides even though only the persistent player realistically ever
        // visits a Bone Altar.
        player.resetBones();
        opponent.resetBones();
        grantBonusStartingBones(player);
        grantBonusStartingBones(opponent);

        // Squirrels are meant to feel effectively unlimited, not a resource
        // that runs dry - top the player's squirrel deck back up to a full
        // 30 before every fight. Opponent-side squirrels aren't topped up
        // here - the opponent Player object is thrown away after the battle
        // regardless, so there's nothing lasting to keep full.
        player.topUpSquirrelDeck();
    }

    private void grantBonusStartingBones(Player side) {
        if (side.getBonusStartingBones() > 0) {
            side.gainBones(side.getBonusStartingBones());
        }
    }

    /**
     * Plays the battle to completion. Returns true if the player won, false
     * if the player was defeated or quit mid-battle (a run should end early
     * on either of those, not just on defeat).
     */
    public boolean play() {
        ui.show("=== " + player.getName() + " vs " + opponent.getName() + " ===");
        if (scanner != null) {
            printHelp(); // the text-command help text is irrelevant in GUI mode, where there are no commands to type
        }

        player.dealOpeningHand(2);
        opponent.dealOpeningHand(2);

        HealthScale scale = engine.getHealthScale();
        boolean surrendered = false;
        while (!quit && !scale.isPlayerVictorious() && !scale.isOpponentVictorious()) {
            if (scanner != null) {
                playerTurn();
            } else {
                playerTurnViaClicks();
            }
            if (quit || scale.isPlayerVictorious()) {
                break;
            }
            if (opponentHasNothingLeft()) {
                if (promptAcceptSurrender()) {
                    surrendered = true;
                    ui.show("You accept the surrender!");
                    break;
                }
                ui.show("You press on. The opponent still has nothing new to bring, "
                    + "but anything already on their board can still fight.");
            }
            opponentTurn();
        }

        boolean won = scale.isPlayerVictorious() || surrendered;
        if (scale.isOpponentVictorious()) {
            ui.show("You were defeated.");
        } else if (surrendered) {
            ui.show("You win by surrender! No bonus teeth this time - there was no real combat finish to earn them from.");
        } else if (won) {
            ui.show("You win! The opponent was defeated.");
        } else {
            ui.show("You left the battle.");
        }
        if (!surrendered) {
            printGoldenTeeth(scale);
            player.gainTeeth(scale.getPlayerGoldenTeeth());
            opponent.gainTeeth(scale.getOpponentGoldenTeeth());
        }
        reclaimSurvivingCards();
        ui.clearBoard();
        return won && !quit;
    }

    /**
     * Any player card still standing on the board when the battle ends -
     * placed and survived, never having died - goes back into hand rather
     * than being silently lost along with this Battle's own Board object
     * once play() returns. The board's graveyard (cards that died during
     * this fight) is reclaimed the same way, so a card that died still
     * eventually finds its way back into the deck rather than being
     * permanently lost - "used" and "drawn" are treated the same way,
     * regardless of whether the card survived or died, both genuinely
     * returned rather than quietly disappearing. Combined with PathRunner's
     * own reshuffleHandIntoDecks() (called right after this node resolves),
     * everything reclaimed here ends up back in the deck for later.
     */
    private void reclaimSurvivingCards() {
        for (ReadOnlySlot slot : board.getPlayerSlots()) {
            Card occupant = slot.getOccupant();
            if (occupant != null) {
                occupant.heal(occupant.getMaxHealth()); // may have taken partial damage without dying - restore to full, not just dead cards
                occupant.resetExhaustion(); // it likely attacked at least once during the fight - a hand card should never show as exhausted
                player.addToHand(occupant);
            }
        }
        for (Card deadCard : board.getPlayerGraveyard()) {
            deadCard.heal(deadCard.getMaxHealth()); // a dead card's health stays at 0 forever otherwise - it needs restoring to actually be usable again
            deadCard.resetExhaustion();
            player.addToHand(deadCard);
        }
    }

    /**
     * Asks the player whether to accept the opponent's surrender or keep
     * fighting. If declined, this same offer comes back on every
     * subsequent check (as long as the opponent still has nothing) - it's
     * not a one-time "take it or leave it forever" prompt.
     */
    private boolean promptAcceptSurrender() {
        int choice = ui.askChoice("The opponent has nothing left to fight with and offers surrender.",
            List.of("Accept the surrender (win now, no bonus teeth)",
                "Continue fighting (risk further exchanges for a chance at more teeth)"));
        return choice == 0;
    }

    /** True once the opponent has nothing left at all - an empty hand AND both decks empty - meaning it could never act again even if given more turns. */
    private boolean opponentHasNothingLeft() {
        return opponent.getHand().isEmpty() && !opponent.canDraw();
    }

    /** Whether the player explicitly quit this battle, as opposed to being genuinely defeated. */
    public boolean didQuit() {
        return quit;
    }

    private void printGoldenTeeth(HealthScale scale) {
        int earned = scale.getPlayerGoldenTeeth();
        if (earned > 0) {
            ui.show("Golden teeth earned (bonus for overkill damage): " + earned);
        }
    }

    private void playerTurn() {
        engine.startTurn(true);
        hasDrawnThisTurn = false;
        ui.show("----- Your turn -----");
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
                case "status" -> printStatus();
                case "draw" -> handleDraw(tokens);
                case "place" -> handlePlace(tokens);
                case "items" -> printItems();
                case "item" -> handleUseItem(tokens);
                case "bell" -> {
                    ringBellForPlayer();
                    turnOver = true;
                }
                case "quit" -> {
                    quit = true;
                    turnOver = true;
                }
                default -> ui.show("Unknown command. Type 'help' for the list.");
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
            ui.show("Both decks are empty - no draw this turn.");
            return;
        }
        ui.show("You must draw before doing anything else this turn.");
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
                case "status" -> printStatus();
                case "quit" -> quit = true;
                default -> ui.show("You must draw first. Usage: draw <animal|squirrel>");
            }
        }
    }

    private void opponentTurn() {
        if (engine.consumeSkipOpponentNextTurn()) {
            ui.show("----- Opponent's turn -----");
            ui.show("The Hourglass's effect holds - the opponent's turn is skipped entirely.");
            return;
        }
        ui.show("----- Opponent's turn -----");

        // 1) Move cards from the back line to the front line if possible.
        engine.startTurn(false);
        engine.promoteReserveCards();
        printBoard();

        // 2) Front line attacks.
        ui.show("*ring* Opponent's creatures attack.");
        engine.ringBell(false);
        ui.show("Opponent acted. " + describeScale());
        printBoard();

        // 3) Put cards on the back line.
        opponentAI.takeTurn(engine, board, opponent, player, ui);
        printBoard();

        // 4) Finish the step.
    }

    private void handleDraw(String[] tokens) {
        if (hasDrawnThisTurn) {
            ui.show("You've already drawn this turn.");
            return;
        }
        if (tokens.length < 2) {
            ui.show("Usage: draw <animal|squirrel>");
            return;
        }
        DeckType type = switch (tokens[1].toLowerCase()) {
            case "animal" -> DeckType.ANIMAL;
            case "squirrel" -> DeckType.SQUIRREL;
            default -> null;
        };
        if (type == null) {
            ui.show("Usage: draw <animal|squirrel>");
            return;
        }
        try {
            Card drawn = player.drawFromDeck(type);
            hasDrawnThisTurn = true;
            ui.show("Drew: " + drawn);
        } catch (NoSuchElementException e) {
            ui.show("That deck is empty.");
        }
    }

    private void handlePlace(String[] tokens) {
        if (tokens.length < 3) {
            ui.show("Usage: place <hand index> <slot index>");
            return;
        }
        Integer handIndex = parseInt(tokens[1]);
        Integer slotIndex = parseInt(tokens[2]);
        if (handIndex == null || slotIndex == null) {
            ui.show("Usage: place <hand index> <slot index>");
            return;
        }
        List<Card> hand = player.getHand();
        if (handIndex < 0 || handIndex >= hand.size()) {
            ui.show("No card at hand index " + handIndex + ". Type 'hand' to see your hand.");
            return;
        }
        Card card = hand.get(handIndex);

        // Check the slot is free before spending anything, so a failed
        // placement never costs the player resources.
        ReadOnlySlot[] playerSlots = board.getPlayerSlots();
        if (slotIndex < 0 || slotIndex >= playerSlots.length) {
            ui.show("Invalid slot index: " + slotIndex);
            return;
        }
        if (!playerSlots[slotIndex].isEmpty()) {
            ui.show("Slot " + slotIndex + " is already occupied by "
                + playerSlots[slotIndex].getOccupant().getName() + ".");
            return;
        }

        payCostAndPlace(card, slotIndex);
    }

    /**
     * Shared by both the console's text-parsed handlePlace() and the
     * click-based handlePlaceViaClicks() - once a card and an already-known
     * -empty slot are settled on, paying for it and actually placing it
     * works identically either way.
     */
    private void payCostAndPlace(Card card, int slotIndex) {
        if (!payCost(card)) {
            return;
        }
        List<Item> itemsBefore = new ArrayList<>(player.getItems());
        engine.placeCard(true, slotIndex, card);
        player.removeFromHand(card);
        ui.show("Placed " + card.getName() + " in slot " + slotIndex + ".");
        announceNewItems(itemsBefore);
        printBoard();
    }

    /**
     * Compares the player's item list before and after some action (a
     * placement whose sigil might grant an item, like Trinket Bearer) and
     * announces anything new that showed up - matching how GainConsumablesEvent
     * announces its own grants ("X added to your inventory."), but detected
     * here rather than the sigil printing anything itself, since a sigil has
     * no display channel of its own and shouldn't need one just for this.
     */
    private void announceNewItems(List<Item> itemsBefore) {
        for (Item item : player.getItems()) {
            if (!itemsBefore.contains(item)) {
                ui.show(item.getName() + " added to your inventory.");
            }
        }
    }

    /**
     * Pays a card's cost in isolation, without placing it - used by the
     * click-based flow, which pays first and only then asks for a lane
     * (the reverse order from the console's handlePlace(), which already
     * knows both the card and the slot before it starts paying). Returns
     * whether the cost was successfully paid.
     */
    private boolean payCost(Card card) {
        if (card.getCost() == 0) {
            return true;
        }
        if (card.getCostType() == ResourceType.BONES) {
            try {
                player.spendResource(ResourceType.BONES, card.getCost());
                return true;
            } catch (InsufficientResourcesException e) {
                ui.show(e.getMessage());
                return false;
            }
        }
        return collectBloodForPlacement(card.getCost());
    }

    /**
     * Guides the player through sacrificing board creatures to cover a
     * Blood cost. Blood is an original game mechanic that never banks - it
     * only ever exists for the instant between sacrificing enough
     * creatures and spending it on the specific card being placed right
     * now, in this same action.
     * <p>
     * Marks (selects) creatures one at a time without sacrificing them
     * immediately. The moment the marked total meets or exceeds the amount
     * needed, every marked creature is sacrificed together as one atomic
     * batch, any overshoot Blood (only possible via a Worthy Sacrifice
     * creature, since an ordinary sacrifice is worth exactly 1) is
     * discarded rather than banked, and the caller proceeds to place the
     * card - there is no going back once the threshold is met.
     * <p>
     * Before the threshold is met, the only way out is backing out of the
     * whole placement - since nothing has actually been sacrificed yet at
     * that point (only marked), nothing is lost by cancelling.
     * <p>
     * Returns true once enough Blood has been collected and spent, false
     * if the player backed out or ran out of creatures to mark first.
     */
    private boolean collectBloodForPlacement(int amountNeeded) {
        List<Integer> marked = new ArrayList<>();
        int markedTotal = 0;
        ReadOnlySlot[] playerSlots = board.getPlayerSlots();

        while (markedTotal < amountNeeded) {
            List<Integer> availableLanes = new ArrayList<>();
            for (int i = 0; i < playerSlots.length; i++) {
                Card occupant = playerSlots[i].getOccupant();
                if (occupant != null && !marked.contains(i) && !occupant.hasSigil("Unsacrificeable")) {
                    availableLanes.add(i);
                }
            }
            if (availableLanes.isEmpty()) {
                ui.show("Not enough creatures left to sacrifice for the remaining "
                    + (amountNeeded - markedTotal) + " blood needed - placement cancelled.");
                return false;
            }

            List<String> options = new ArrayList<>();
            for (int lane : availableLanes) {
                options.add("Lane " + lane + ": " + playerSlots[lane].getOccupant());
            }
            options.add("Cancel");
            int choice = ui.askChoice(
                "Need " + (amountNeeded - markedTotal) + " more blood. Mark a creature to sacrifice"
                    + " (nothing marked so far has actually been sacrificed):",
                options);
            if (choice == options.size() - 1) {
                ui.show("Placement cancelled - nothing was sacrificed.");
                return false;
            }
            int lane = availableLanes.get(choice);
            marked.add(lane);
            Card occupant = playerSlots[lane].getOccupant();
            markedTotal += occupant.hasSigil("Worthy Sacrifice") ? 3 : 1;
        }

        for (int lane : marked) {
            engine.sacrificeFromBoard(true, lane);
        }
        String wastedNote = markedTotal > amountNeeded ? " (" + (markedTotal - amountNeeded) + " blood wasted)" : "";
        ui.show("Sacrificed " + marked.size() + " creature(s) for " + markedTotal + " blood" + wastedNote + ".");
        return true;
    }

    private void printItems() {
        List<Item> items = player.getItems();
        if (items.isEmpty()) {
            ui.show("Items: (none)");
            return;
        }
        StringBuilder sb = new StringBuilder("Items:");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            sb.append("\n  [").append(i).append("] ").append(item.getName()).append(" - ").append(item.getDescription());
        }
        ui.show(sb.toString());
    }

    private void handleUseItem(String[] tokens) {
        List<Item> items = player.getItems();
        if (tokens.length < 2) {
            ui.show("Usage: item <index>");
            printItems();
            return;
        }
        Integer index = parseInt(tokens[1]);
        if (index == null || index < 0 || index >= items.size()) {
            ui.show("No item at that index. Type 'items' to see what you're carrying.");
            return;
        }
        Item item = items.get(index);
        boolean consumed = item.use(new ItemContext(engine, board, player, opponent, scanner));
        if (consumed) {
            player.removeItem(item);
            ui.show(item.getName() + " was used up.");
        }
        printBoard();
    }

    private void ringBellForPlayer() {
        ui.show("*ring* Combat begins - your creatures attack left to right.");
        engine.ringBell(true);
        printBoard();
        ui.show(describeScale());
    }

    private Integer parseInt(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void printHelp() {
        ui.show("""
            Commands:
              hand                       show your hand (with indices)
              status                     show the scale / blood / bones / hand / board
              draw animal|squirrel       mandatory once per turn, before anything else
              place <hand#> <slot#>      play a card from hand onto a board slot
                                         (pays its {cost: N blood/bones} if it has one -
                                         a blood cost may prompt you to sacrifice cards
                                         to cover it, right there in the same action)
              items                      show items you're carrying
              item <index>               use one of your items
              bell                       ring the bell: your creatures attack left to
                                         right (hitting face if a lane is undefended),
                                         then it's the opponent's turn
              quit                       leave the battle
            """);
    }

    private void printStatus() {
        ui.show(describeScale() + " | Bones: " + player.getBones()
            + " | Items: " + player.getItems().size() + "/" + com.inscription.player.Player.MAX_ITEMS);
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
        ui.show(hand.isEmpty() ? "Hand: (empty)" : sb.toString());
    }

    private void printBoard() {
        ReadOnlySlot[] playerSlots = board.getPlayerSlots();
        ReadOnlySlot[] opponentSlots = board.getOpponentSlots();
        ReadOnlySlot[] opponentReserveSlots = board.getOpponentReserveSlots();

        List<String> playerLanes = new ArrayList<>();
        List<String> opponentAttackingLanes = new ArrayList<>();
        for (int i = 0; i < playerSlots.length; i++) {
            playerLanes.add(describeSlot(playerSlots[i]));
            opponentAttackingLanes.add(describeSlot(opponentSlots[i]));
        }
        List<String> opponentReserveLanes = new ArrayList<>();
        for (ReadOnlySlot slot : opponentReserveSlots) {
            opponentReserveLanes.add(describeSlot(slot));
        }

        ui.updateBoard(new BoardSnapshot(playerLanes, opponentAttackingLanes, opponentReserveLanes));
        ui.updateBones(player.getBones());
    }

    private String describeSlot(ReadOnlySlot slot) {
        Card occupant = slot.getOccupant();
        if (occupant == null) {
            return "(empty)";
        }
        return occupant.describe(engine.getDisplayAttack(occupant, true));
    }

    // ---- Click-based combat loop, used only when scanner == null (a GUI context) ----
    //
    // The console's text-command loop above genuinely cannot work here: there's no
    // way for a GUI to "type a command string." Everything below reaches the exact
    // same outcomes (draw, place a card - including the guided sacrifice flow via
    // the shared collectBloodForPlacement()/payCostAndPlace(), use an item, ring the
    // bell, quit) purely through GameUI.askChoice() calls instead.

    private void playerTurnViaClicks() {
        engine.startTurn(true);
        hasDrawnThisTurn = false;
        ui.show("----- Your turn -----");
        printStatus();

        requireDrawViaClicks();
        if (quit) {
            return;
        }

        boolean turnOver = false;
        while (!turnOver && !quit) {
            List<String> actions = new ArrayList<>();
            actions.add("Place a card");
            if (!player.getItems().isEmpty()) {
                actions.add("Use an item");
            }
            actions.add("Ring the bell (end turn)");
            actions.add("Quit");

            int choice = ui.askChoice("What would you like to do?", actions);
            String chosen = actions.get(choice);

            switch (chosen) {
                case "Place a card" -> handlePlaceViaClicks();
                case "Use an item" -> handleUseItemViaClicks();
                case "Ring the bell (end turn)" -> {
                    ringBellForPlayer();
                    turnOver = true;
                }
                case "Quit" -> {
                    quit = true;
                    turnOver = true;
                }
                default -> throw new IllegalStateException("Unhandled action: " + chosen);
            }
        }
    }

    private void requireDrawViaClicks() {
        if (!player.canDraw()) {
            ui.show("Both decks are empty - no draw this turn.");
            return;
        }
        while (!hasDrawnThisTurn && !quit) {
            int choice = ui.askChoice("You must draw before doing anything else this turn:",
                List.of("Draw Animal", "Draw Squirrel", "Quit"));
            if (choice == 2) {
                quit = true;
                return;
            }
            DeckType type = choice == 0 ? DeckType.ANIMAL : DeckType.SQUIRREL;
            try {
                Card drawn = player.drawFromDeck(type);
                hasDrawnThisTurn = true;
                ui.show("Drew: " + drawn);
            } catch (NoSuchElementException e) {
                ui.show("That deck is empty - try the other one.");
            }
        }
    }

    private void handlePlaceViaClicks() {
        List<Card> hand = player.getHand();
        if (hand.isEmpty()) {
            ui.show("Your hand is empty.");
            return;
        }
        List<String> handOptions = new ArrayList<>();
        for (Card c : hand) {
            handOptions.add(c.describe(engine.getDisplayAttack(c, true)));
        }
        handOptions.add("Cancel");
        int handChoice = ui.askChoice("Choose a card to place:", handOptions);
        if (handChoice == handOptions.size() - 1) {
            return;
        }
        Card card = hand.get(handChoice);

        // Checked upfront, before paying anything: if there's no way this
        // placement could ever end up with an empty lane, there'd be no
        // point paying at all. A Bones cost never sacrifices anything, so
        // it strictly needs an already-empty lane. A Blood cost sacrifices
        // creatures to pay - and normally that always frees a lane, since a
        // sacrificed creature usually dies. The one exception is Many
        // Lives: sacrificing one of those never frees its lane, since the
        // creature survives. So a Blood-cost card is only truly blocked
        // when EVERY occupied lane is Many Lives, with no empty lane and no
        // mortal sacrifice available either - not just whenever the board
        // happens to be full.
        boolean blocked;
        if (card.getCostType() == ResourceType.BONES) {
            blocked = !hasEmptyLane();
        } else {
            blocked = !hasEmptyLane() && !hasSacrificeableLane();
        }
        if (blocked) {
            ui.show("No empty lane to place a card in.");
            return;
        }

        if (!payCost(card)) {
            return; // player backed out, or couldn't afford it - nothing sacrificed, nothing placed
        }

        // Choose the lane AFTER paying, since sacrificing for Blood may have
        // freed up more lanes than were available when this all started.
        ReadOnlySlot[] playerSlots = board.getPlayerSlots();
        List<Integer> emptyLanes = new ArrayList<>();
        for (int i = 0; i < playerSlots.length; i++) {
            if (playerSlots[i].isEmpty()) {
                emptyLanes.add(i);
            }
        }
        List<String> laneOptions = new ArrayList<>();
        for (int lane : emptyLanes) {
            laneOptions.add("Lane " + lane);
        }
        if (laneOptions.isEmpty()) {
            ui.show("The sacrifices didn't actually free up a lane (Many Lives creatures survive) - "
                + card.getName() + " stays in hand.");
            return;
        }
        int laneChoice = ui.askChoice("Choose a lane for " + card.getName() + ":", laneOptions);
        int slotIndex = emptyLanes.get(laneChoice);

        List<Item> itemsBefore = new ArrayList<>(player.getItems());
        engine.placeCard(true, slotIndex, card);
        player.removeFromHand(card);
        ui.show("Placed " + card.getName() + " in slot " + slotIndex + ".");
        announceNewItems(itemsBefore);
        printBoard();
    }

    private boolean hasEmptyLane() {
        for (ReadOnlySlot slot : board.getPlayerSlots()) {
            if (slot.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Whether any occupied lane holds a creature that would actually free up its lane if sacrificed - i.e., doesn't have Many Lives, which lets the creature survive sacrifice in place. */
    private boolean hasSacrificeableLane() {
        for (ReadOnlySlot slot : board.getPlayerSlots()) {
            Card occupant = slot.getOccupant();
            if (occupant != null && !occupant.hasSigil("Many Lives")) {
                return true;
            }
        }
        return false;
    }

    private void handleUseItemViaClicks() {
        List<Item> items = player.getItems();
        if (items.isEmpty()) {
            ui.show("You have no items.");
            return;
        }
        List<String> options = new ArrayList<>();
        for (Item item : items) {
            options.add(item.getName() + " - " + item.getDescription());
        }
        options.add("Cancel");
        int choice = ui.askChoice("Choose an item to use:", options);
        if (choice == options.size() - 1) {
            return;
        }
        Item item = items.get(choice);
        boolean consumed = item.use(new ItemContext(engine, board, player, opponent, ui));
        if (consumed) {
            player.removeItem(item);
            ui.show(item.getName() + " was used up.");
        }
        printBoard();
    }
}
