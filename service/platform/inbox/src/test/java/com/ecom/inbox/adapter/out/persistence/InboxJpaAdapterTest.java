package com.ecom.inbox.adapter.out.persistence;

import com.ecom.inbox.port.spi.InboxStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class InboxJpaAdapterTest {

    private InboxStore inboxStore;

    @Autowired
    private InboxRepository inboxRepository;

    @BeforeEach
    void setUp() {
        // Clear the table before each test
        inboxRepository.deleteAll();
        // Construct the adapter
        inboxStore = new InboxJpaAdapter(inboxRepository);
    }

    @Test
    void firstEventForAnAggregateIsNew() {
        // Act: check if it is a duplicate
        boolean isDuplicate = inboxStore.isDuplicate("event-1", "Product", "prod-1");

        // Assert: the event is not a duplicate
        assertThat(isDuplicate).isFalse();

        // Act: save the event
        inboxStore.saveEvent("event-1", "Product", "prod-1");

        // Assert: the event is stored in the database
        InboxEntity entity = inboxRepository.findById(new InboxEntity.InboxId("Product", "prod-1")).orElseThrow();
        assertThat(entity.getLastEventId()).isEqualTo("event-1");
    }

    @Test
    void sameEventIdAgainIsADuplicate() {
        // Arrange: save the first event
        inboxStore.saveEvent("event-1", "Product", "prod-1");

        // Act: check the same event again
        boolean isDuplicate = inboxStore.isDuplicate("event-1", "Product", "prod-1");

        // Assert: it is recognized as a duplicate
        assertThat(isDuplicate).isTrue();
    }

    @Test
    void aDifferentEventIdOverwrites() {
        // Arrange: save the first event
        inboxStore.saveEvent("event-1", "Product", "prod-1");

        // Act: check a different event for the same aggregate
        boolean isDuplicate = inboxStore.isDuplicate("event-2", "Product", "prod-1");

        // Assert: it is not a duplicate
        assertThat(isDuplicate).isFalse();

        // Act: save the new event
        inboxStore.saveEvent("event-2", "Product", "prod-1");

        // Assert: the last event id is overwritten
        InboxEntity entity = inboxRepository.findById(new InboxEntity.InboxId("Product", "prod-1")).orElseThrow();
        assertThat(entity.getLastEventId()).isEqualTo("event-2");
    }

    @Test
    void sameEventIdStoredForOneAggregateIsNotADuplicateForADifferentAggregateOfTheSameType() {
        // Arrange: save the event for the first aggregate
        inboxStore.saveEvent("event-1", "Product", "prod-1");

        // Act: check the same event id for a different aggregate of the same type
        boolean isDuplicate = inboxStore.isDuplicate("event-1", "Product", "prod-2");

        // Assert: it is not considered a duplicate for the different aggregate
        assertThat(isDuplicate).isFalse();
    }
}
