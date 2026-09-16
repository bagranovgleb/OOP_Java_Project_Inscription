package com.inscription.model;

/**
 * A non-creature board occupant - Dam, Boulder, Tail - built to sit and
 * block a lane, never to attack. A real subclass rather than just "a Card
 * with attack 0 by convention": both onAttack() and buffAttack() are
 * overridden to be no-ops, so an obstacle can never actually deal damage or
 * even display a nonzero attack, no matter what touches it later (a totem,
 * a campfire buff, a stitched sigil transfer) - a guarantee enforced by
 * polymorphism, not just a printed number a future feature could quietly
 * break.
 * <p>
 * Attack isn't a constructor parameter at all, for the same reason - there's
 * no way to build one that starts with nonzero attack in the first place.
 */
public class ObstacleCard extends Card {

    public ObstacleCard(String name, int health, int cost, ResourceType costType) {
        super(name, 0, health, cost, costType);
    }

    @Override
    public void onAttack(Card target, int attackAmount) {
        // Obstacles never deal damage, regardless of the amount the engine
        // computed - blocking is the whole point, attacking isn't possible.
    }

    @Override
    public void buffAttack(int amount) {
        // Refuses silently rather than throwing - a totem or campfire simply
        // has no effect on an obstacle's attack, since it can never have any.
    }
}
