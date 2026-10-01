package com.ecom.shared.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SharedExceptionsTest {

    @Test
    void resourceNotFoundKeepsMessageAndIsUnchecked() {
        // Build the exception with a message
        ResourceNotFoundException ex = new ResourceNotFoundException("missing");

        // The message is kept and no try/catch is forced on callers
        assertEquals("missing", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void unauthorizedAccessKeepsMessageAndIsUnchecked() {
        // Build the exception with a message
        UnauthorizedAccessException ex = new UnauthorizedAccessException("denied");

        // The message is kept and the exception is unchecked
        assertEquals("denied", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void invalidRequestKeepsMessageAndIsUnchecked() {
        // Build the exception with a message
        InvalidRequestException ex = new InvalidRequestException("bad");

        // The message is kept and the exception is unchecked
        assertEquals("bad", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
