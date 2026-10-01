package com.ecom.inbox;

import com.ecom.contract.DomainEvent;
import com.ecom.contract.EventCatalog;
import com.ecom.contract.EventEnvelope;
import com.ecom.inbox.port.spi.InboxStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

public class InboxReceiver {


    private static final Logger log = LoggerFactory.getLogger(InboxReceiver.class);

    private final InboxStore inboxStore;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public InboxReceiver(InboxStore inboxStore, ObjectMapper objectMapper, ApplicationEventPublisher eventPublisher) {
        this.inboxStore = inboxStore;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    // Process the inbox message in a single transaction
    @Transactional
    public void receive(InboxMessage message) {
        // Read the aggregate's row with a lock; if last_event_id equals the message's eventId it is a duplicate
        if (inboxStore.isDuplicate(message.eventId(), message.aggregateType(), message.aggregateId())) {
            log.debug("Duplicate event, skipping: {}", message.eventId());
            return;
        }

        try {
            // Decode the envelope from the payload
            EventEnvelope envelope = objectMapper.readValue(message.payload(), EventEnvelope.class);

            // Resolve the event record class using the EventCatalog
            Optional<Class<? extends DomainEvent>> recordClassOpt = EventCatalog.resolve(envelope.aggregate(), envelope.action());

            if (recordClassOpt.isEmpty()) {
                // An unknown aggregate or action: log a warning, do not dispatch
                log.warn("Unknown aggregate or action: {}/{}", envelope.aggregate(), envelope.action());
            } else {
                // Parse the event data into the specific record
                DomainEvent event = objectMapper.convertValue(envelope.data(), recordClassOpt.get());
                // Publish it to synchronous @EventListener methods
                eventPublisher.publishEvent(event);
            }

            // Store the new eventId and processed_at timestamp to acknowledge
            inboxStore.saveEvent(message.eventId(), message.aggregateType(), message.aggregateId());

        } catch (JsonProcessingException e) {
            // A listener failure or parsing error propagates and rolls back the transaction
            log.error("Failed to process inbox message {}", message.eventId(), e);
            throw new RuntimeException("Failed to process inbox message", e);
        }
    }
}
