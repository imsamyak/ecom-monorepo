package com.ecom.outbox;

import com.ecom.contract.DomainEvent;
import com.ecom.outbox.entity.OutboxEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.Objects;

/**
 * Event description returned by {@link OutboxUseCase#buildOutbox}. Validated by the outbox aspect, which fails
 * (and rolls back) the use case if it is invalid. The payload is serialized to JSON by the aspect.
 */
@Value
@Builder
public class Outbox {

    @NotBlank
    @Size(max = OutboxEntity.MAX_FIELD_LENGTH)
    String aggregateType;

    /** Optional key of the aggregate the event belongs to; useful as a partition/ordering key. */
    @Size(min = 1, max = OutboxEntity.MAX_FIELD_LENGTH)
    String aggregateId;

    @NotNull
    Object payload;

    /**
     * Builds the outbox row from a domain event so use cases never spell out type and id by hand. The aggregate type
     * is the event record's simple name; the aggregate id is whatever string the event returns.
     */
    public static Outbox of(DomainEvent event) {
        // Refuse a missing event up front
        Objects.requireNonNull(event, "event must not be null");

        // Anonymous classes and lambdas have no usable name, so they cannot supply an aggregate type
        Class<?> type = event.getClass();
        if (type.isAnonymousClass() || type.isSynthetic()) {
            throw new IllegalArgumentException("DomainEvent must be a named class or record: " + type.getName());
        }

        // The record's own name is the aggregate type
        String aggregateType = type.getSimpleName();

        // The record already converted its id to a string; only reject an empty one
        String aggregateId = event.aggregateId();
        if (aggregateId == null || aggregateId.isBlank()) {
            throw new IllegalArgumentException(aggregateType + ".aggregateId() must not be null or blank");
        }

        // The event itself is the payload, so Jackson serializes the record's components
        return Outbox.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload(event)
                .build();
    }
}
