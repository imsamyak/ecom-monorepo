package com.ecom.shared.validation;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SelfValidatingTest {

    // Sample command that validates itself at the end of its constructor
    private static class Command extends SelfValidating<Command> {
        @NotBlank(message = "name is required")
        final String name;
        @Positive(message = "amount must be positive")
        final int amount;

        Command(String name, int amount) {
            this.name = name;
            this.amount = amount;
            validateSelf();
        }
    }

    @Test
    void validObjectConstructsWithoutError() {
        // Valid values pass self validation silently
        assertDoesNotThrow(() -> new Command("ok", 1));
    }

    @Test
    void invalidObjectThrowsConstraintViolationExceptionFromItsConstructor() {
        // A blank name breaks exactly one rule
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> new Command(" ", 1));

        // The single violation carries the message declared on the field
        assertEquals(1, ex.getConstraintViolations().size());
        assertEquals("name is required", ex.getConstraintViolations().iterator().next().getMessage());
    }

    @Test
    void reportsEveryViolationNotJustTheFirst() {
        // Both fields are invalid at once
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> new Command("", 0));

        // Both violations are reported together
        assertEquals(2, ex.getConstraintViolations().size());
    }
}
