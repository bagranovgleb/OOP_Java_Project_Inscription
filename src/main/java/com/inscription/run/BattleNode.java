package com.inscription.run;

import com.inscription.cli.Battle;
import com.inscription.player.Player;

import java.util.function.Function;

/**
 * A battle node on the path. The opponent is built fresh each time this
 * node is reached (in case a path ever loops or a node is revisited), via
 * a factory that receives the player - letting the opponent's deck be
 * sized against the player's current deck (see RunGame.enemyFromDeck())
 * rather than being a fixed size regardless of how the run has gone so
 * far. Each fight gets its own new Board/GameEngine/HealthScale - only the
 * player (and their deck/hand) carries through from one battle to the next.
 */
public class BattleNode implements NodeContent {

    private final String name;
    private final Function<Player, Player> opponentFactory;
    private final boolean isSnowLevel;

    public BattleNode(String name, Function<Player, Player> opponentFactory) {
        this(name, opponentFactory, false);
    }

    /** Same as the 2-arg version, but flags this as a Snowline level - passed through to Battle so Grand Fir becomes Snowy Fir. */
    public BattleNode(String name, Function<Player, Player> opponentFactory, boolean isSnowLevel) {
        this.name = name;
        this.opponentFactory = opponentFactory;
        this.isSnowLevel = isSnowLevel;
    }

    @Override
    public String describe() {
        return "Battle: " + name;
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();
        Player opponent = opponentFactory.apply(player);
        Battle battle = context.getScanner() != null
            ? new Battle(player, opponent, context.getScanner(), isSnowLevel)
            : new Battle(player, opponent, context.getUI(), isSnowLevel);
        boolean won = battle.play();
        if (won) {
            return true;
        }

        // Quitting mid-battle is treated the same as a genuine defeat - it
        // forfeits just this fight, not the whole run. A candle is lost,
        // and the run continues afterward if any remain.
        player.loseCandle();
        if (player.isOutOfCandles()) {
            context.getUI().show("Your last candle goes out. The run ends here.");
            return false;
        }
        context.getUI().show("A candle goes out - " + player.getCandles() + " remaining. You press on.");
        return true;
    }
}
