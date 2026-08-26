package com.inscription.model;

import com.inscription.sigil.FrozenAwaySigil;
import com.inscription.sigil.Sigil;

import java.util.List;
import java.util.function.Supplier;

/**
 * Cards that can only ever be given to a player by a sigil's effect or a run
 * event - never drawn from a normal deck, never available for deck-building.
 * A deliberately separate enum from CardType, not just a comment section
 * within it, so nothing that loops over the normal catalog (e.g. building a
 * starting deck) could ever accidentally include one of these by mistake.
 * <p>
 * TODO - unresolved icons/effects, no sigils attached yet: Boulder and
 * Rabbit are plain (no icon). The Smoke carries a claw/hand icon, meaning
 * unconfirmed.
 */
public enum SpecialCardType {

    BOULDER("Boulder", 0, 5, 0, ResourceType.BLOOD, List.of()),

    FROZEN_OPOSSUM("Frozen Opossum", 0, 5, 0, ResourceType.BLOOD,
        List.of(() -> new FrozenAwaySigil(CardType.OPOSSUM::create))),

    RABBIT("Rabbit", 0, 1, 0, ResourceType.BLOOD, List.of()),

    THE_SMOKE("The Smoke", 0, 1, 0, ResourceType.BLOOD, List.of()),

    /** The decoy Loose Tail leaves behind in its place when it escapes. */
    TAIL("Tail", 0, 1, 0, ResourceType.BLOOD, List.of());

    private final String cardName;
    private final int attack;
    private final int health;
    private final int cost;
    private final ResourceType costType;
    private final List<Supplier<Sigil>> sigilFactories;

    SpecialCardType(String cardName, int attack, int health, int cost, ResourceType costType,
                     List<Supplier<Sigil>> sigilFactories) {
        this.cardName = cardName;
        this.attack = attack;
        this.health = health;
        this.cost = cost;
        this.costType = costType;
        this.sigilFactories = sigilFactories;
    }

    /**
     * Builds a fresh Card instance for this catalog entry. Unlike
     * CardType.create(), this does not set Card.sourceType - a "make a copy
     * of me" sigil (e.g. Unkillable) simply isn't supported on a special
     * card yet, since none of these currently need it.
     */
    public Card create() {
        Card card = new Card(cardName, attack, health, cost, costType);
        for (Supplier<Sigil> factory : sigilFactories) {
            card.addSigil(factory.get());
        }
        return card;
    }
}
