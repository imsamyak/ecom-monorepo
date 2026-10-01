package com.ecom.outbox;

import com.ecom.outbox.entity.OutboxEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

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
}
