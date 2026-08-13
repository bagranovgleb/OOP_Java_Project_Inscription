package com.inscryptor.exception;

/** Thrown when two active ChallengeModifiers are declared incompatible with each other. */
public class ConflictingModifierException extends RuntimeException {
    public ConflictingModifierException(String message) {
        super(message);
    }
}
