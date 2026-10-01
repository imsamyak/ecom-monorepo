package com.ecom.contract.event;

import com.ecom.contract.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Events of the Product aggregate. The interface name is the aggregate type and each nested record is an action, so
 * the set of actions is closed. All actions share one aggregate id rule: the product id.
 */
public sealed interface Product extends DomainEvent {

    UUID productId();

    // Every product event is keyed by the product id, so all events of one product stay ordered together
    @Override
    default String aggregateId() {
        return productId().toString();
    }

    /** A product was created; carries the full snapshot. */
    record Created(UUID productId, UUID sellerId, String title, String description, double price,
                   LocalDateTime createdAt, LocalDateTime updatedAt) implements Product {
    }

    /** A product was updated; carries the full snapshot after the change. */
    record Updated(UUID productId, UUID sellerId, String title, String description, double price,
                   LocalDateTime createdAt, LocalDateTime updatedAt) implements Product {
    }

    /** A product was deleted; only the identifiers remain. */
    record Deleted(UUID productId, UUID sellerId) implements Product {
    }
}
