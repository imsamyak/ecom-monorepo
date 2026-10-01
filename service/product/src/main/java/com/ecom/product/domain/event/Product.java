package com.ecom.product.domain.event;

import com.ecom.contract.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event describing a product. Named Product on purpose: the record name is the aggregate type used by the outbox.
 * Its fields are the same as ProductResult, so the published JSON payload keeps its shape.
 */
public record Product(
        UUID id,
        UUID sellerId,
        String title,
        String description,
        double price,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements DomainEvent {

    // The event is about one product, so its id, converted to a string, is the aggregate id
    @Override
    public String aggregateId() {
        return id.toString();
    }
}
