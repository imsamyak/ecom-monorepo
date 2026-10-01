package com.ecom.contract.event;

import com.ecom.contract.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Events of the Variant aggregate. A variant belongs to a product, so its events are keyed by the owning product id
 * (not the variant id): variant events then stay ordered together with the events of that product.
 */
public sealed interface Variant extends DomainEvent {

    UUID productId();

    // Key by the owning product so variant events share the product's partition
    @Override
    default String aggregateId() {
        return productId().toString();
    }

    /** A variant was added to a product. */
    record ADD(Long variantId, UUID productId, Map<String, String> properties,
                 LocalDateTime createdAt, LocalDateTime updatedAt) implements Variant {
    }

    /** A variant was removed from a product; carries what was removed. */
    record REMOVE(Long variantId, UUID productId, Map<String, String> properties) implements Variant {
    }
}
