package com.example.LearnAssist.Exceptions;

import com.example.LearnAssist.Configurations.ExceptionError;

/**
 * Thrown when an uploaded file is rejected (bad extension, MIME type, content or size).
 * Extends ExceptionError so existing controllers keep answering 400 with the message.
 */
public class InvalidFileException extends ExceptionError {
    public InvalidFileException(String message) {
        super(message);
    }
}
