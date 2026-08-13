package com.inscryptor.model;

/**
 * A tougher, named opponent card. Overrides onAttack to show polymorphism in
 * action: the engine still just calls onAttack(target), but a boss can deal
 * bonus damage without any special-casing in the engine.
 */
public class BossCard extends Card {

    private final int bonusDamage;

    public BossCard(String name, int attack, int health, int bonusDamage, int cost, ResourceType costType) {
        super(name, attack, health, cost, costType);
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void onAttack(Card target) {
        target.takeDamage(this.attack + bonusDamage);
    }
}
