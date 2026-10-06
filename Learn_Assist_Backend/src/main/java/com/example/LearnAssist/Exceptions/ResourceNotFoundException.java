package com.example.LearnAssist.Exceptions;

/**
 * Thrown when a resource does not exist OR is not visible to the current user.
 * Using the same exception for both cases avoids revealing which ids exist.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
