package com.ecom.contract;

import com.ecom.contract.event.ProductEvent;
import com.ecom.contract.event.VariantEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCatalogTest {

    @Test
    void resolvesProductCreate() {
        // Resolve the action CREATE for the aggregate Product
        Optional<Class<? extends DomainEvent>> type = EventCatalog.resolve("Product", "CREATE");

        // Verify it resolves precisely to the ProductEvent.CREATE record class
        assertTrue(type.isPresent());
        assertEquals(ProductEvent.CREATE.class, type.get());
    }

    @Test
    void resolvesProductUpdate() {
        // Resolve the action UPDATE for the aggregate Product
        Optional<Class<? extends DomainEvent>> type = EventCatalog.resolve("Product", "UPDATE");

        // Verify it resolves to the ProductEvent.UPDATE record class
        assertTrue(type.isPresent());
        assertEquals(ProductEvent.UPDATE.class, type.get());
    }

    @Test
    void resolvesProductDelete() {
        // Resolve the action DELETE for the aggregate Product
        Optional<Class<? extends DomainEvent>> type = EventCatalog.resolve("Product", "DELETE");

        // Verify it resolves to the ProductEvent.DELETE record class
        assertTrue(type.isPresent());
        assertEquals(ProductEvent.DELETE.class, type.get());
    }

    @Test
    void resolvesVariantAdd() {
        // Resolve the action ADD for the aggregate Variant
        Optional<Class<? extends DomainEvent>> type = EventCatalog.resolve("Variant", "ADD");

        // Verify it resolves to the VariantEvent.ADD record class
        assertTrue(type.isPresent());
        assertEquals(VariantEvent.ADD.class, type.get());
    }

    @Test
    void resolvesVariantRemove() {
        // Resolve the action REMOVE for the aggregate Variant
        Optional<Class<? extends DomainEvent>> type = EventCatalog.resolve("Variant", "REMOVE");

        // Verify it resolves to the VariantEvent.REMOVE record class
        assertTrue(type.isPresent());
        assertEquals(VariantEvent.REMOVE.class, type.get());
    }

    @Test
    void unknownAggregateGivesEmptyResult() {
        // Attempt to resolve an action for an unknown aggregate type
        Optional<Class<? extends DomainEvent>> type = EventCatalog.resolve("Unknown", "CREATE");

        // Verify it fails safely by returning empty rather than throwing
        assertTrue(type.isEmpty());
    }

    @Test
    void unknownActionGivesEmptyResult() {
        // Attempt to resolve an unknown action for a known aggregate type
        Optional<Class<? extends DomainEvent>> type = EventCatalog.resolve("Product", "UNKNOWN");

        // Verify it fails safely by returning empty rather than throwing
        assertTrue(type.isEmpty());
    }
}
