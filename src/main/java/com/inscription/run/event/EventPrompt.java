package com.inscription.run.event;

import java.util.Scanner;

/** Shared input-reading helper for run events that present numbered choices. */
final class EventPrompt {

    private EventPrompt() {
    }

    static int readChoice(Scanner scanner, int optionCount) {
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= 0 && choice < optionCount) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the reminder below
            }
            System.out.println("Enter a number between 0 and " + (optionCount - 1) + ".");
        }
    }
}
