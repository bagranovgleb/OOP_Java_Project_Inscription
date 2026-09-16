package com.inscription.run;

import com.inscription.player.Player;
import com.inscription.ui.GameUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Walks the player along a path from a starting node to wherever it ends,
 * resolving each node's content and letting the player choose between
 * options whenever a node has more than one possible next step. After each
 * node resolves, shows the player's progress along the path and waits for
 * an explicit "Go Forward" before advancing, rather than auto-continuing.
 */
public class PathRunner {

    private final RunContext context;

    public PathRunner(Player player, Scanner scanner) {
        this.context = new RunContext(player, scanner);
    }

    /** For a GUI context, which has no real Scanner to speak of - see RunContext's matching constructor. */
    public PathRunner(Player player, GameUI ui) {
        this.context = new RunContext(player, ui);
    }

    /** Runs the whole path. Returns true if the player reached the end, false if the run ended early (defeat/quit). */
    public boolean run(PathNode start) {
        List<PathNode> fullSequence = enumerateSequence(start);
        PathNode current = start;
        int position = 0;
        while (current != null) {
            context.getUI().show("########## " + current.getContent().describe() + " ##########");
            boolean survived = current.getContent().resolve(context);
            if (!survived) {
                context.getUI().show("Your run has ended.");
                return false;
            }

            // Reclaims hand cards back into their decks after every event or fight -
            // keeps the deck from running dry over a long run from repeated draws
            // (Deck Trial's permanent 3-card draw, a battle's own draws, etc.),
            // without inventing cards from nowhere: only what the player still
            // genuinely owns (sitting in hand) comes back.
            context.getPlayer().reshuffleHandIntoDecks();

            printProgress(fullSequence, position);

            List<PathNode> options = current.getNextOptions();
            if (options.isEmpty()) {
                context.getUI().show("You've reached the end of the path. Run complete!");
                return true;
            }

            context.getUI().waitForContinue("[Go Forward] Press Enter to continue...");
            current = chooseNext(options);
            position++;
        }
        return true;
    }

    /**
     * Walks the path from start, always taking the first listed option at
     * each node, to build a full ordered view for the progress display.
     * Accurate for the purely linear paths this project currently builds
     * (e.g. Location 1). If a genuinely branching path were ever used, this
     * would only preview the first branch at each choice point - a
     * reasonable approximation for a progress display, not a claim about
     * what's definitely ahead.
     */
    private List<PathNode> enumerateSequence(PathNode start) {
        List<PathNode> sequence = new ArrayList<>();
        PathNode current = start;
        while (current != null) {
            sequence.add(current);
            List<PathNode> options = current.getNextOptions();
            current = options.isEmpty() ? null : options.get(0);
        }
        return sequence;
    }

    /** [x] for completed steps, [>] for the one just resolved, [ ] for what's still ahead. */
    private void printProgress(List<PathNode> fullSequence, int currentIndex) {
        StringBuilder sb = new StringBuilder("Your path:");
        for (int i = 0; i < fullSequence.size(); i++) {
            String marker = i < currentIndex ? "[x]" : i == currentIndex ? "[>]" : "[ ]";
            sb.append("\n  ").append(marker).append(" ").append(fullSequence.get(i).getContent().describe());
        }
        context.getUI().show(sb.toString());
    }

    private PathNode chooseNext(List<PathNode> options) {
        if (options.size() == 1) {
            return options.get(0);
        }
        List<String> optionTexts = options.stream().map(o -> o.getContent().describe()).toList();
        int choice = context.getUI().askChoice("Choose your next step:", optionTexts);
        return options.get(choice);
    }
}
