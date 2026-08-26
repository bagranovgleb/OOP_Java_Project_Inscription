package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

import java.util.function.Supplier;

/**
 * After surviving until its owner's next turn, this card grows into a
 * stronger form - either literally, by transforming into a different named
 * card, or generically, by permanently gaining +1 attack / +2 health while
 * staying the same card. Which mode a given card uses is a constructor
 * choice, since it depends on whether that card has a distinct "grown form"
 * in the catalog (Raven Egg -> Raven, Wolf Cub -> Wolf) or not.
 * <p>
 * Triggers on the very first TURN_START its owner's side fires after this
 * card is placed. A card is only ever placed mid-turn (after that turn's
 * own TURN_START has already fired via engine.startTurn()), so the next
 * TURN_START it ever receives necessarily comes at the start of its owner's
 * NEXT turn - meaning the opponent has already had a turn in between, so
 * "survived 1 turn" has genuinely happened by the time this fires. A guard
 * flag keeps this to exactly one trigger even for the stat-buff variant,
 * where the same card object survives and would otherwise keep receiving
 * TURN_START events indefinitely.
 */
public class FledglingSigil implements Sigil {

    private final Supplier<Card> strongerForm;
    private final int attackGain;
    private final int healthGain;
    private boolean alreadyGrown;

    /** Transform variant: e.g. {@code new FledglingSigil(CardType.RAVEN::create)}. */
    public FledglingSigil(Supplier<Card> strongerForm) {
        this.strongerForm = strongerForm;
        this.attackGain = 0;
        this.healthGain = 0;
    }

    /** Generic stat-buff variant: stays the same card, permanently gains stats. */
    public FledglingSigil(int attackGain, int healthGain) {
        this.strongerForm = null;
        this.attackGain = attackGain;
        this.healthGain = healthGain;
    }

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (alreadyGrown || event.getType() != GameEventType.TURN_START || event.getSource() != owner) {
            return;
        }
        alreadyGrown = true;

        if (strongerForm != null) {
            int[] location = locationOf(owner, context);
            if (location == null) {
                return;
            }
            boolean isPlayerSide = location[0] == 1;
            int lane = location[1];
            context.getBoard().removeCard(isPlayerSide, lane);
            context.getBoard().placeCard(isPlayerSide, lane, strongerForm.get());
        } else {
            owner.buffAttack(attackGain);
            owner.buffMaxHealth(healthGain);
        }
    }

    @Override
    public String getName() {
        return "Fledgling";
    }
}
