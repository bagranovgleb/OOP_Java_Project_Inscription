package com.inscription.exception;

/** Thrown when a player tries to spend more blood/bones than they have. */
public class InsufficientResourcesException extends RuntimeException {
    public InsufficientResourcesException(String message) {
        super(message);
    }
}
