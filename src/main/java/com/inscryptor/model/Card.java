package com.inscryptor.model;

import com.inscryptor.engine.GameContext;
import com.inscryptor.sigil.Sigil;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every creature card in the game.
 * <p>
 * Encapsulation: attack/health bookkeeping is private/protected state that can
 * only be changed through the methods below (takeDamage, heal, sacrifice, move) -
 * nothing outside this class can mutate health directly.
 * <p>
 * Abstraction: callers (the engine, sigils) never need to know which concrete
 * subclass or which sigils a card has - they just call applySigils()/onAttack()
 * and the right behavior happens.
 */
public class Card {

    protected final String name;
    protected int attack;
    protected int health;
    protected final int maxHealth;
    protected boolean exhausted;
    protected final int cost;
    protected final ResourceType costType;

    private final List<Sigil> sigils = new ArrayList<>();

    protected Card(String name, int attack, int health, int cost, ResourceType costType) {
        if (attack < 0 || health <= 0) {
            throw new IllegalArgumentException("Attack must be >= 0 and health > 0");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }
        this.name = name;
        this.attack = attack;
        this.health = health;
        this.maxHealth = health;
        this.cost = cost;
        this.costType = costType;
    }

    public void addSigil(Sigil sigil) {
        sigils.add(sigil);
    }

    /** Returns a read-only view; callers cannot add/remove sigils from outside. */
    public List<Sigil> getSigils() {
        return List.copyOf(sigils);
    }

    /** Same as getSigils(), but excludes hidden identity sigils - for UI display. */
    public List<Sigil> getVisibleSigils() {
        return sigils.stream().filter(sigil -> !sigil.isHidden()).toList();
    }

    /**
     * Generic identity/effect check by sigil name, so code like totem modifiers
     * can ask "does this card carry sigil X" without an instanceof chain over
     * concrete card subclasses.
     */
    public boolean hasSigil(String sigilName) {
        for (Sigil sigil : sigils) {
            if (sigil.getName().equals(sigilName)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public int getAttack() {
        return attack;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    /** How much of costType this card takes to place on the board. 0 means free. */
    public int getCost() {
        return cost;
    }

    /** Which resource pays for this card. Irrelevant when cost is 0. */
    public ResourceType getCostType() {
        return costType;
    }

    public boolean isExhausted() {
        return exhausted;
    }

    public boolean isAlive() {
        return health > 0;
    }

    /** True unless a sigil (e.g. a submerged Diver) is actively blocking targeting. */
    public boolean canBeTargeted() {
        for (Sigil sigil : sigils) {
            if (sigil.preventsTargeting(this)) {
                return false;
            }
        }
        return true;
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount cannot be negative");
        }
        health = Math.min(maxHealth, health + amount);
    }

    /** Permanently increases attack (e.g. a totem buffing every card of a species). */
    public void buffAttack(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Attack buff cannot be negative");
        }
        attack += amount;
    }

    /** Kills the card outright; used when the player sacrifices it for resources. */
    public void sacrifice() {
        health = 0;
    }

    public void move() {
        exhausted = true;
    }

    public void resetExhaustion() {
        exhausted = false;
    }

    /**
     * Default attack behavior: deal this card's attack as damage to the target.
     * Polymorphism: subclasses (e.g. a boss with a multi-hit attack) can override
     * this instead of the engine special-casing card types.
     */
    public void onAttack(Card target) {
        target.takeDamage(this.attack);
    }

    /**
     * Applies every sigil this card carries for the given event. The engine calls
     * this without knowing which (if any) sigils are attached - that's the point
     * of routing everything through the Sigil interface.
     */
    public final void applySigils(GameEvent event, GameContext context) {
        for (Sigil sigil : sigils) {
            sigil.apply(event, this, context);
        }
    }

    @Override
    public String toString() {
        String costPart = cost > 0
            ? String.format(" {cost: %d %s}", cost, costType.name().toLowerCase())
            : "";
        return String.format("%s [%d/%d]%s%s", name, attack, health, costPart, exhausted ? " (exhausted)" : "");
    }
}
