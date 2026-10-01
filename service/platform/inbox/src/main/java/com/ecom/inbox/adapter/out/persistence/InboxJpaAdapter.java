package com.ecom.inbox.adapter.out.persistence;

import com.ecom.inbox.port.spi.InboxStore;
import java.time.Instant;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InboxJpaAdapter implements InboxStore {

    private final InboxRepository inboxRepository;

    public InboxJpaAdapter(InboxRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    @Override
    public boolean isDuplicate(String eventId, String aggregateType, String aggregateId) {
        // Lock the row to prevent concurrent processing of the same aggregate
        Optional<InboxEntity> entity = inboxRepository.findByAggregateWithLock(aggregateType, aggregateId);
        
        // Return true if the event id matches the last processed event
        return entity.isPresent() && entity.get().getLastEventId().equals(eventId);
    }

    @Override
    public void saveEvent(String eventId, String aggregateType, String aggregateId) {
        // Find existing or create a new entity
        InboxEntity entity = inboxRepository.findById(new InboxEntity.InboxId(aggregateType, aggregateId))
                .orElseGet(() -> new InboxEntity(aggregateType, aggregateId));
        
        // Update the event details
        entity.setLastEventId(eventId);
        entity.setProcessedAt(Instant.now());
        
        // Save to the database
        inboxRepository.save(entity);
    }
}
