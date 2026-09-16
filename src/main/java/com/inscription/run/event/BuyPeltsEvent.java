package com.inscription.run.event;

import com.inscription.pelt.PeltKind;
import com.inscription.player.Player;
import com.inscription.run.NodeContent;
import com.inscription.run.RunContext;

import java.util.List;

/**
 * Buy Pelts: exchange teeth for pelts with the Trapper. The first Rabbit
 * Pelt is granted unconditionally the moment the player arrives here -
 * regardless of how many teeth they have, and regardless of what they buy
 * or whether they buy anything at all - not something they have to
 * specifically choose from the market before it counts. After that,
 * Rabbit costs 2 teeth, Wolf 4, Golden Sheep 8. Repeatable within one
 * visit - keep buying while there's something affordable, or leave
 * whenever.
 */
public class BuyPeltsEvent implements NodeContent {

    private static final int RABBIT_COST = 2;
    private static final int WOLF_COST = 4;
    private static final int SHEEP_COST = 8;

    @Override
    public String describe() {
        return "Buy Pelts: exchange teeth for pelts with the Trapper";
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();

        if (!player.hasReceivedFreeRabbitPelt()) {
            player.markFreeRabbitPeltReceived();
            player.gainPelt(PeltKind.RABBIT);
            context.getUI().show("Your first Rabbit Pelt is on the house.");
        }

        while (true) {
            List<String> options = List.of(
                "Rabbit Pelt (" + RABBIT_COST + " teeth)",
                "Wolf Pelt (" + WOLF_COST + " teeth)",
                "Golden Sheep Pelt (" + SHEEP_COST + " teeth)",
                "Leave");
            int choice = context.getUI().askChoice(
                "You have " + player.getTeeth() + " teeth. What would you like to buy?", options);

            if (choice == 3) {
                return true;
            }

            PeltKind kind = switch (choice) {
                case 0 -> PeltKind.RABBIT;
                case 1 -> PeltKind.WOLF;
                default -> PeltKind.SHEEP;
            };
            int cost = switch (choice) {
                case 0 -> RABBIT_COST;
                case 1 -> WOLF_COST;
                default -> SHEEP_COST;
            };

            if (!player.spendTeeth(cost)) {
                context.getUI().show("Not enough teeth for that.");
                continue;
            }
            player.gainPelt(kind);
            context.getUI().show("Bought a " + kind.getDisplayName() + ".");
        }
    }
}
