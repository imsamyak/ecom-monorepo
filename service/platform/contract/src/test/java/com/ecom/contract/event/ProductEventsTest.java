package com.ecom.contract.event;

import com.ecom.contract.DomainEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductEventsTest {

    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

    private ProductEvent.CREATE created() {
        return new ProductEvent.CREATE(productId, sellerId, "Phone", "desc", 10.5, now, now, "INACTIVE");
    }

    private ProductEvent.UPDATE updated() {
        return new ProductEvent.UPDATE(productId, sellerId, "Phone 2", "desc 2", 11.5, now, now, "ACTIVE");
    }

    private ProductEvent.DELETE deleted() {
        return new ProductEvent.DELETE(productId, sellerId);
    }

    @Test
    void productIsASealedDomainEventWithExactlyTheThreeActions() {
        // The set of actions is closed, so consumers can handle every one of them
        assertTrue(ProductEvent.class.isSealed());
        assertTrue(DomainEvent.class.isAssignableFrom(ProductEvent.class));
        Set<String> actions = Arrays.stream(ProductEvent.class.getPermittedSubclasses())
                .map(Class::getSimpleName).collect(Collectors.toSet());
        assertEquals(Set.of("CREATE", "UPDATE", "DELETE"), actions);
    }

    @Test
    void everyActionIsAProductEvent() {
        // Each record is both a Product event and a DomainEvent
        assertInstanceOf(ProductEvent.class, created());
        assertInstanceOf(ProductEvent.class, updated());
        assertInstanceOf(ProductEvent.class, deleted());
    }

    @Test
    void everyActionUsesTheProductIdAsAggregateId() {
        // One id rule shared by all actions: the product id as a string
        assertEquals(productId.toString(), created().aggregateId());
        assertEquals(productId.toString(), updated().aggregateId());
        assertEquals(productId.toString(), deleted().aggregateId());
    }

    @Test
    void aggregateTypeIsTheInterfaceNameAndActionIsTheRecordName() {
        // The outbox reads these by reflection, so pin where they come from
        assertEquals("ProductEvent", created().getClass().getDeclaringClass().getSimpleName());
        assertEquals("CREATE", created().getClass().getSimpleName());
        assertEquals("UPDATE", updated().getClass().getSimpleName());
        assertEquals("DELETE", deleted().getClass().getSimpleName());
    }

    @Test
    void createdAndUpdatedCarryTheFullProductSnapshot() {
        // The data a consumer needs to rebuild the product
        ProductEvent.CREATE c = created();
        assertEquals(productId, c.productId());
        assertEquals(sellerId, c.sellerId());
        assertEquals("Phone", c.title());
        assertEquals("desc", c.description());
        assertEquals(10.5, c.price());
        assertEquals(now, c.createdAt());
        assertEquals(now, c.updatedAt());
        assertEquals("INACTIVE", c.status());
        assertEquals("Phone 2", updated().title());
        assertEquals("ACTIVE", updated().status());
    }

    @Test
    void deletedCarriesOnlyTheIdentifiers() {
        // Nothing else is known or needed once the product is gone
        ProductEvent.DELETE d = deleted();
        assertEquals(productId, d.productId());
        assertEquals(sellerId, d.sellerId());
        assertEquals(2, ProductEvent.DELETE.class.getRecordComponents().length);
    }
}
