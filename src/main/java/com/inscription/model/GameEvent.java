package com.inscription.model;

/**
 * A single occurrence in the game (an attack, a death, a turn starting...)
 * that gets broadcast to cards so their sigils can react. Immutable on purpose -
 * nothing downstream should be able to rewrite what already happened.
 */
public final class GameEvent {

    private final GameEventType type;
    private final Card source;
    private final Card target;

    public GameEvent(GameEventType type, Card source, Card target) {
        this.type = type;
        this.source = source;
        this.target = target;
    }

    public GameEventType getType() {
        return type;
    }

    /** The card that caused the event (e.g. the attacker). May be null. */
    public Card getSource() {
        return source;
    }

    /** The card the event happened to (e.g. the defender). May be null. */
    public Card getTarget() {
        return target;
    }
}
