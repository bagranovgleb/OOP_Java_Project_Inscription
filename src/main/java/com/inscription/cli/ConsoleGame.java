package com.inscription.cli;

import com.inscription.deck.Deck;
import com.inscription.deck.StarterDecks;
import com.inscription.model.CardType;
import com.inscription.player.Player;

import java.util.List;
import java.util.Scanner;

/**
 * Standalone single-battle entry point: builds one player with the beginner
 * deck, one opponent, and plays exactly one Battle - the same experience
 * this class always offered. For a full branching run with events between
 * fights, see RunGame instead; both share the same underlying Battle class,
 * so nothing about how a fight actually plays differs between the two.
 */
public class ConsoleGame {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Player player = new Player("You", StarterDecks.beginnerAnimalDeck(), StarterDecks.beginnerSquirrelDeck());
        Player opponent = new Player("Opponent", new Deck(List.of(
            CardType.WOLF.create(), CardType.WOLF.create(), CardType.PORCUPINE.create())),
            new Deck(List.of(CardType.SQUIRREL.create(), CardType.SQUIRREL.create(), CardType.SQUIRREL.create())));

        new Battle(player, opponent, scanner).play();
        scanner.close();
    }
}
