package com.sarthak.universityManagement.seed;

// Thrown for anything wrong with a seed file or run. It fails startup, so the message must say what to fix.
public class SeedException extends RuntimeException {
    public SeedException(String message) {
        super(message);
    }

    public SeedException(String message, Throwable cause) {
        super(message, cause);
    }
}
