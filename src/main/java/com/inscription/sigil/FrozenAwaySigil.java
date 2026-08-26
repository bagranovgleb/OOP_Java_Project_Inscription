package com.inscription.sigil;

import com.inscription.engine.GameContext;
import com.inscription.model.Card;
import com.inscription.model.GameEvent;
import com.inscription.model.GameEventType;

import java.util.function.Supplier;

/**
 * When this card perishes, the creature inside takes its place - removed
 * from whatever lane it died in, and a freshly built replacement card is
 * placed there instead. Which creature is "inside" is data, not behavior, so
 * it's a constructor parameter rather than a hardcoded card - e.g. Frozen
 * Opossum would be built with {@code new FrozenAwaySigil(CardType.OPOSSUM::create)}.
 * <p>
 * Verified side effect: because the replacement happens immediately (before
 * GameEngine's bone-crediting scan runs), the original card's death does
 * NOT grant its owner the usual 1 bone - the slot is already refilled by
 * the time that scan looks at it. Treated as intentional here (this is a
 * transformation, not really a "death"), but worth knowing if that ever
 * feels wrong for a specific card.
 */
public class FrozenAwaySigil implements Sigil {

    private final Supplier<Card> releasedCreature;

    public FrozenAwaySigil(Supplier<Card> releasedCreature) {
        this.releasedCreature = releasedCreature;
    }

    @Override
    public void apply(GameEvent event, Card owner, GameContext context) {
        if (event.getType() != GameEventType.DEATH || event.getSource() != owner) {
            return;
        }
        int[] location = locationOf(owner, context);
        if (location == null) {
            return;
        }
        boolean isPlayerSide = location[0] == 1;
        int lane = location[1];
        context.getBoard().removeCard(isPlayerSide, lane);
        context.getBoard().placeCard(isPlayerSide, lane, releasedCreature.get());
    }

    @Override
    public String getName() {
        return "Frozen Away";
    }
}
