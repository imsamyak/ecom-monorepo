package com.ecom.inbox.cleaner;

import com.ecom.inbox.adapter.out.persistence.InboxEntity;
import com.ecom.inbox.adapter.out.persistence.InboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class InboxCleanerTest {

    private InboxCleaner inboxCleaner;

    @Autowired
    private InboxRepository inboxRepository;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        inboxRepository.deleteAll();
        // Construct the cleaner with a 7-day TTL
        inboxCleaner = new InboxCleaner(inboxRepository, Duration.ofDays(7));
    }

    @Test
    void rowsOlderThanTheTtlAreDeletedByTheCleanerAndNewerOnesKept() {
        // Arrange: create an old event (8 days ago)
        InboxEntity oldEntity = new InboxEntity("Product", "prod-old");
        oldEntity.setLastEventId("event-1");
        oldEntity.setProcessedAt(Instant.now().minus(Duration.ofDays(8)));
        inboxRepository.save(oldEntity);

        // Arrange: create a new event (6 days ago)
        InboxEntity newEntity = new InboxEntity("Product", "prod-new");
        newEntity.setLastEventId("event-2");
        newEntity.setProcessedAt(Instant.now().minus(Duration.ofDays(6)));
        inboxRepository.save(newEntity);

        // Act: run the cleaner
        inboxCleaner.cleanup();

        // Assert: the old entity is deleted
        assertThat(inboxRepository.findById(new InboxEntity.InboxId("Product", "prod-old"))).isEmpty();

        // Assert: the new entity is kept
        assertThat(inboxRepository.findById(new InboxEntity.InboxId("Product", "prod-new"))).isPresent();
    }
}
