package com.ecom.outbox;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxConstraintsTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static Outbox.OutboxBuilder valid() {
        return Outbox.builder().aggregateType("Product").aggregateId("42").payload("x");
    }

    @Test
    void acceptsValidOutbox() {
        assertTrue(validator.validate(valid().build()).isEmpty());
        assertTrue(validator.validate(valid().aggregateId(null).build()).isEmpty());
    }

    @Test
    void rejectsBadFields() {
        assertEquals(1, validator.validate(valid().aggregateType(null).build()).size());
        assertEquals(1, validator.validate(valid().aggregateType(" ").build()).size());
        assertEquals(1, validator.validate(valid().aggregateType("a".repeat(101)).build()).size());
        assertEquals(1, validator.validate(valid().aggregateId("").build()).size());
        assertEquals(1, validator.validate(valid().aggregateId("a".repeat(101)).build()).size());
        assertEquals(1, validator.validate(valid().payload(null).build()).size());
    }
}
