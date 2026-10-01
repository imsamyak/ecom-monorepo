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

    private Variant.Added added() {
        return new Variant.Added(7L, productId, Map.of("color", "red"), now, now);
    }

    private Variant.Removed removed() {
        return new Variant.Removed(7L, productId, Map.of("color", "red"));
    }

    @Test
    void variantIsASealedDomainEventWithExactlyTheTwoActions() {
        // The set of actions is closed
        assertTrue(Variant.class.isSealed());
        assertTrue(DomainEvent.class.isAssignableFrom(Variant.class));
        Set<String> actions = Arrays.stream(Variant.class.getPermittedSubclasses())
                .map(Class::getSimpleName).collect(Collectors.toSet());
        assertEquals(Set.of("Added", "Removed"), actions);
    }

    @Test
    void everyActionIsAVariantEvent() {
        // Each record is a Variant event
        assertInstanceOf(Variant.class, added());
        assertInstanceOf(Variant.class, removed());
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
        assertEquals("Variant", added().getClass().getDeclaringClass().getSimpleName());
        assertEquals("Added", added().getClass().getSimpleName());
        assertEquals("Removed", removed().getClass().getSimpleName());
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
