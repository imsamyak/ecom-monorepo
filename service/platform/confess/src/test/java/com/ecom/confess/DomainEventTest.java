package com.ecom.confess;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainEventTest {

    // Sample events whose aggregate id is a different type each time
    private record WithUuid(UUID id, String name) implements DomainEvent {
        @Override
        public Object aggregateId() {
            return id;
        }
    }

    private record WithLong(long number) implements DomainEvent {
        @Override
        public Object aggregateId() {
            return number;
        }
    }

    private record WithString(String code) implements DomainEvent {
        @Override
        public Object aggregateId() {
            return code;
        }
    }

    @Test
    void interfaceHasExactlyOneAbstractMethodCalledAggregateId() {
        // Collect every method declared on the interface
        Method[] methods = DomainEvent.class.getDeclaredMethods();

        // The contract stays minimal: one method, abstract (not default), named aggregateId
        assertEquals(1, methods.length, "DomainEvent must stay a one-method contract: " + Arrays.toString(methods));
        assertEquals("aggregateId", methods[0].getName());
        assertTrue(Modifier.isAbstract(methods[0].getModifiers()), "aggregateId must not be a default method");
        assertEquals(0, methods[0].getParameterCount());
    }

    @Test
    void aRecordCanChooseAUuidAttributeAsTheAggregateId() {
        // The record picks its id component
        UUID id = UUID.randomUUID();

        // The chosen attribute comes back untouched, with its original type
        assertEquals(id, new WithUuid(id, "x").aggregateId());
    }

    @Test
    void aRecordCanChooseANumericAttributeAsTheAggregateId() {
        // A primitive component is boxed when returned as Object
        assertEquals(42L, new WithLong(42L).aggregateId());
    }

    @Test
    void aRecordCanChooseAStringAttributeAsTheAggregateId() {
        // A string component is returned as is
        assertEquals("SKU-1", new WithString("SKU-1").aggregateId());
    }

    @Test
    void aLambdaCanImplementItBecauseItIsASingleMethodInterface() {
        // Only one abstract method means a lambda is a valid implementation
        DomainEvent event = () -> "lambda-id";

        // It behaves like any other implementation
        assertEquals("lambda-id", event.aggregateId());
    }
}
