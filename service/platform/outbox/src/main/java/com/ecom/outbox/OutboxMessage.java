package com.ecom.outbox;

import java.util.UUID;

/** What the relay hands to an {@link com.ecom.outbox.relay.OutboxPublisher}. {@code id} is stable across retries (dedupe key). */
public record OutboxMessage(UUID id, String aggregateType, String aggregateId, String payload) {
}
