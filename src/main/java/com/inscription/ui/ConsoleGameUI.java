package com.inscription.ui;

import java.util.List;
import java.util.Scanner;

/**
 * Console implementation of GameUI - prints to standard out and blocks on
 * Scanner input, reproducing exactly what every event's own println/Scanner
 * calls used to do directly, before this abstraction existed.
 */
public class ConsoleGameUI implements GameUI {

    private final Scanner scanner;

    public ConsoleGameUI(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void show(String message) {
        System.out.println(message);
    }

    @Override
    public int askChoice(String prompt, List<String> options) {
        System.out.println(prompt);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  [" + i + "] " + options.get(i));
        }
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= 0 && choice < options.size()) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the reminder below
            }
            System.out.println("Enter a number between 0 and " + (options.size() - 1) + ".");
        }
    }

    @Override
    public void waitForContinue(String prompt) {
        System.out.print(prompt);
        scanner.nextLine();
    }
}
