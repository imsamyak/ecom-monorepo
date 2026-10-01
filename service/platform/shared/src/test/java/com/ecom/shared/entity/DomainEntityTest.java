package com.ecom.shared.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainEntityTest {

    // Minimal subclass that exposes the protected JPA lifecycle callbacks to the test
    private static class Sample extends DomainEntity {
        void persist() { onPrePersist(); }
        void update() { onPreUpdate(); }
    }

    @Test
    void timestampsAreNullBeforeFirstPersist() {
        // A brand new entity has not been saved, so it has no timestamps yet
        Sample entity = new Sample();

        // Both timestamps stay empty until the persist callback runs
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void prePersistSetsCreatedAtAndUpdatedAtToNowInUtc() {
        // Remember the time window around the callback
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC);
        Sample entity = new Sample();

        // Simulate the first save
        entity.persist();
        LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);

        // Both timestamps are set and fall inside the window
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertFalse(entity.getCreatedAt().isBefore(before));
        assertFalse(entity.getCreatedAt().isAfter(after));
        assertFalse(entity.getUpdatedAt().isBefore(before));
        assertFalse(entity.getUpdatedAt().isAfter(after));
    }

    @Test
    void preUpdateChangesUpdatedAtButNeverCreatedAt() throws InterruptedException {
        // Save once and remember both timestamps
        Sample entity = new Sample();
        entity.persist();
        LocalDateTime createdAt = entity.getCreatedAt();
        LocalDateTime firstUpdatedAt = entity.getUpdatedAt();

        // Wait so the clock moves forward before the update
        Thread.sleep(5);
        entity.update();

        // The creation time is untouched while the update time moved forward
        assertEquals(createdAt, entity.getCreatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(firstUpdatedAt));
    }
}
