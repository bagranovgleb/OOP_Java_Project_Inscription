package com.inscription.model;

/** The kinds of events the engine broadcasts to cards' sigils. */
public enum GameEventType {
    ATTACK,
    DEATH,
    TURN_START,
    TURN_END,
    MOVE,
    PLACE
}
