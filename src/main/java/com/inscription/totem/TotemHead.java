package com.inscription.totem;

/** A totem head - defines which tribe an assembled totem applies to. */
public class TotemHead implements TotemPiece {

    private final String tribe;

    public TotemHead(String tribe) {
        this.tribe = tribe;
    }

    public String getTribe() {
        return tribe;
    }

    @Override
    public String describe() {
        return "Head (" + tribe + ")";
    }
}
