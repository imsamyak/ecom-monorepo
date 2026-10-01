package com.ecom.contract.event;

import com.ecom.contract.DomainEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantEventsTest {

    private final UUID productId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

    private VariantEvent.ADD added() {
        return new VariantEvent.ADD(7L, productId, Map.of("color", "red"), now, now);
    }

    private VariantEvent.REMOVE removed() {
        return new VariantEvent.REMOVE(7L, productId, Map.of("color", "red"));
    }

    @Test
    void variantIsASealedDomainEventWithExactlyTheTwoActions() {
        // The set of actions is closed
        assertTrue(VariantEvent.class.isSealed());
        assertTrue(DomainEvent.class.isAssignableFrom(VariantEvent.class));
        Set<String> actions = Arrays.stream(VariantEvent.class.getPermittedSubclasses())
                .map(Class::getSimpleName).collect(Collectors.toSet());
        assertEquals(Set.of("ADD", "REMOVE"), actions);
    }

    @Test
    void everyActionIsAVariantEvent() {
        // Each record is a Variant event
        assertInstanceOf(VariantEvent.class, added());
        assertInstanceOf(VariantEvent.class, removed());
    }

    @Test
    void aggregateIdIsTheOwningProductIdNotTheVariantId() {
        // Variant events share the product's partition key so they stay ordered with that product's events
        assertEquals(productId.toString(), added().aggregateId());
        assertEquals(productId.toString(), removed().aggregateId());
        assertNotEquals("7", added().aggregateId());
    }

    @Test
    void aggregateTypeIsTheInterfaceNameAndActionIsTheRecordName() {
        // The outbox reads these by reflection
        assertEquals("VariantEvent", added().getClass().getDeclaringClass().getSimpleName());
        assertEquals("ADD", added().getClass().getSimpleName());
        assertEquals("REMOVE", removed().getClass().getSimpleName());
    }

    @Test
    void addedAndRemovedBothCarryTheVariantIdAndItsProperties() {
        // A consumer can tell which variant, of which product, with which properties
        assertEquals(7L, added().variantId());
        assertEquals(Map.of("color", "red"), added().properties());
        assertEquals(7L, removed().variantId());
        assertEquals(Map.of("color", "red"), removed().properties());
    }
}
