package com.inscription.run;

import com.inscription.cli.Battle;
import com.inscription.model.Card;
import com.inscription.model.SpecialCardType;
import com.inscription.player.Player;
import com.inscription.sigil.ConjuredMarker;

import java.util.function.Function;

/**
 * A boss fight: the player is capped to exactly 1 candle for the whole
 * encounter (even if they arrived with more), and must defeat the boss
 * across 2 separate, fresh battles in a row - the boss effectively has 2
 * "candles" of its own, one snuffed out per round won. Losing either round
 * costs the player's one remaining candle, ending the run immediately -
 * unlike a regular BattleNode, there's no continuing after a boss loss.
 * <p>
 * Right before the encounter begins (once, not per round), The Smoke is
 * placed directly into the player's hand - a permanent, near-worthless
 * card that's always available for the fight ahead, rather than being
 * shuffled into the deck where it might not be drawn for several turns.
 * dealOpeningHand() only ever adds cards on top of what's already in hand
 * (never clears it first), so this survives into round 1's actual opening
 * hand correctly.
 * <p>
 * The opponent factory receives the player (fresh each round), so the
 * boss's deck can be sized against the player's current deck rather than
 * a fixed size.
 */
public class BossBattleNode implements NodeContent {

    private static final int ROUNDS_TO_WIN = 2;

    private final String bossName;
    private final Function<Player, Player> opponentFactory;
    private final boolean isSnowLevel;

    public BossBattleNode(String bossName, Function<Player, Player> opponentFactory) {
        this(bossName, opponentFactory, false);
    }

    /** Same as the 2-arg version, but flags this as a Snowline level - passed through to Battle so Grand Fir becomes Snowy Fir. */
    public BossBattleNode(String bossName, Function<Player, Player> opponentFactory, boolean isSnowLevel) {
        this.bossName = bossName;
        this.opponentFactory = opponentFactory;
        this.isSnowLevel = isSnowLevel;
    }

    @Override
    public String describe() {
        return "Boss: " + bossName;
    }

    @Override
    public boolean resolve(RunContext context) {
        Player player = context.getPlayer();

        if (player.getCandles() > 1) {
            context.getUI().show("As you face " + bossName + ", your extra candle is snuffed out - only one remains.");
            player.setCandles(1);
        }

        Card theSmoke = SpecialCardType.THE_SMOKE.create();
        theSmoke.addSigil(new ConjuredMarker()); // granted for this boss fight specifically - shouldn't linger in the deck afterward
        player.addToHand(theSmoke);
        context.getUI().show("The Smoke settles into your hand, ready for what's ahead.");

        for (int round = 1; round <= ROUNDS_TO_WIN; round++) {
            context.getUI().show("===== " + bossName + " - Round " + round + " of " + ROUNDS_TO_WIN + " =====");
            Player opponent = opponentFactory.apply(player);
            Battle battle = context.getScanner() != null
                ? new Battle(player, opponent, context.getScanner(), isSnowLevel)
                : new Battle(player, opponent, context.getUI(), isSnowLevel);
            boolean won = battle.play();

            if (!won) {
                // Quitting mid-round is treated the same as a genuine defeat -
                // it forfeits the round, not some special free exit. By this
                // point the player is down to their last candle anyway
                // (capped on entering the boss fight), so either way the run
                // ends here - this just keeps the two paths consistent.
                player.loseCandle();
                context.getUI().show("Your last candle goes out. " + bossName + " has bested you - the run ends here.");
                return false;
            }

            boolean isFinalRound = round == ROUNDS_TO_WIN;
            context.getUI().show("Round " + round + " won! " + (isFinalRound
                ? bossName + "'s last candle goes out - victory!"
                : bossName + "'s candle gutters - one remains."));
        }
        return true;
    }
}
