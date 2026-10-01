package com.ecom.outbox.relay;

import com.ecom.outbox.OutboxMessage;

/**
 * Implement as a Spring bean (Kafka, RabbitMQ, ...) to ship outbox rows. Delivery is at-least-once, so
 * consumers should deduplicate on {@link OutboxMessage#id()}.
 */
public interface OutboxPublisher {
    void publish(OutboxMessage message) throws Exception;
}
