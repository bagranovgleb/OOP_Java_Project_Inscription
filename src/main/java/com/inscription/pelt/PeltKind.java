package com.inscription.pelt;

import com.inscription.model.Card;
import com.inscription.model.ResourceType;

import java.util.function.Predicate;

/**
 * The 3 base pelt kinds (bought from the Trapper with teeth) and their
 * fused counterparts (made by fusing 2 of the same base kind with the
 * Mycologists). A fused kind shares its base's market size and cost
 * filter ("same rules for market" per the reference spec), but the Trader
 * shows fused (double-stat) versions of eligible cards for it instead of
 * plain ones - see isFused().
 */
public enum PeltKind {

    RABBIT("Rabbit Pelt", 8, false,
        c -> costMatches(c, ResourceType.BLOOD, 1, 2) || costMatches(c, ResourceType.BONES, 2, 3)),
    WOLF("Wolf Pelt", 8, false,
        c -> costMatches(c, ResourceType.BLOOD, 3, 3) || costMatches(c, ResourceType.BONES, 4, 4)),
    SHEEP("Golden Sheep Pelt", 4, false,
        c -> costMatches(c, ResourceType.BLOOD, 4, 4) || costMatches(c, ResourceType.BONES, 6, Integer.MAX_VALUE)),
    FUSED_RABBIT("Fused Rabbit Pelt", 8, true, RABBIT.costFilter),
    FUSED_WOLF("Fused Wolf Pelt", 8, true, WOLF.costFilter),
    FUSED_SHEEP("Fused Sheep Pelt", 4, true, SHEEP.costFilter);

    private final String displayName;
    private final int marketSize;
    private final boolean fused;
    private final Predicate<Card> costFilter;

    PeltKind(String displayName, int marketSize, boolean fused, Predicate<Card> costFilter) {
        this.displayName = displayName;
        this.marketSize = marketSize;
        this.fused = fused;
        this.costFilter = costFilter;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMarketSize() {
        return marketSize;
    }

    public boolean isFused() {
        return fused;
    }

    public boolean matchesCostFilter(Card card) {
        return costFilter.test(card);
    }

    /** The base (non-fused) kind paired with this one - itself, if this is already a base kind. */
    public PeltKind baseKind() {
        return switch (this) {
            case FUSED_RABBIT -> RABBIT;
            case FUSED_WOLF -> WOLF;
            case FUSED_SHEEP -> SHEEP;
            default -> this;
        };
    }

    /** The fused kind paired with this one - null if this is already a fused kind. */
    public PeltKind fusedKind() {
        return switch (this) {
            case RABBIT -> FUSED_RABBIT;
            case WOLF -> FUSED_WOLF;
            case SHEEP -> FUSED_SHEEP;
            default -> null;
        };
    }

    private static boolean costMatches(Card card, ResourceType type, int min, int max) {
        return card.getCostType() == type && card.getCost() >= min && card.getCost() <= max;
    }
}
