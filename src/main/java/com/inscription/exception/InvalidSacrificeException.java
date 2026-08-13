package com.inscription.exception;

/** Thrown when a player tries to sacrifice a card they don't actually have. */
public class InvalidSacrificeException extends RuntimeException {
    public InvalidSacrificeException(String message) {
        super(message);
    }
}
