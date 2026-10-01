package com.ecom.outbox;

import com.ecom.contract.DomainEvent;
import com.ecom.contract.EventEnvelope;
import com.ecom.outbox.entity.OutboxEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.Objects;

/**
 * Event description returned by {@link OutboxAwareUseCase#buildOutbox}. Validated by the outbox aspect, which fails
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
     * Builds the outbox row from a domain event so use cases never spell out type and id by hand. The event must be a
     * record nested in its aggregate interface (for example {@code Product.CREATE}): the interface name is the
     * aggregate type, the record name is the action, and the aggregate id is the string the event returns. The payload
     * is an {@link EventEnvelope} {aggregate, action, data}.
     */
    public static Outbox of(DomainEvent event) {
        // Refuse a missing event up front
        Objects.requireNonNull(event, "event must not be null");

        // The aggregate type is the interface the record is nested in; anonymous classes, lambdas and top-level
        // records have no such interface, so they cannot say what they belong to
        Class<?> type = event.getClass();
        Class<?> aggregate = type.getDeclaringClass();
        if (type.isAnonymousClass() || type.isSynthetic() || aggregate == null) {
            throw new IllegalArgumentException("DomainEvent must be a record nested in its aggregate interface "
                    + "(for example ProductEvent.CREATE): " + type.getName());
        }
        
        // Derive aggregate type from declaring interface name, expecting 'Event' suffix
        String aggregateInterfaceName = aggregate.getSimpleName();
        if (!aggregateInterfaceName.endsWith("Event") || aggregateInterfaceName.equals("Event")) {
            throw new IllegalArgumentException("Declaring interface name must end with 'Event' and cannot be exactly 'Event': " + aggregateInterfaceName);
        }
        String aggregateType = aggregateInterfaceName.substring(0, aggregateInterfaceName.length() - 5);

        // The record's own name is the action
        String action = type.getSimpleName();

        // The event already converted its id to a string; only reject an empty one
        String aggregateId = event.aggregateId();
        if (aggregateId == null || aggregateId.isBlank()) {
            throw new IllegalArgumentException(aggregateInterfaceName + "." + action + ".aggregateId() must not be null or blank");
        }

        // The payload is the envelope; the event itself is its data, so Jackson serializes the record's components
        return Outbox.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload(new EventEnvelope(aggregateType, action, event))
                .build();
    }
}
