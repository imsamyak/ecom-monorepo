package com.ecom.product.domain.event;

import com.ecom.confess.DomainEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Event describing a product variant. Named Variant on purpose: the record name is the aggregate type used by the
 * outbox. Its fields are the same as VariantResult.
 */
public record Variant(
        Long id,
        UUID productId,
        Map<String, String> properties,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements DomainEvent {

    // A variant belongs to a product, so the owning product id is the aggregate id (the partition key);
    // that keeps variant events ordered together with the events of the same product
    @Override
    public String aggregateId() {
        return productId.toString();
    }
}
