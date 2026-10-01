package com.ecom.outbox.relay;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryBackoffTest {

    private final Instant t0 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void doublesFromOneSecondAndCaps() {
        RetryBackoff backoff = new RetryBackoff(1000, 8000);
        UUID id = UUID.randomUUID();

        assertEquals(1000, backoff.recordFailure(id, t0));
        assertEquals(2000, backoff.recordFailure(id, t0));
        assertEquals(4000, backoff.recordFailure(id, t0));
        assertEquals(8000, backoff.recordFailure(id, t0));
        assertEquals(8000, backoff.recordFailure(id, t0));   // capped
        assertEquals(5, backoff.attempts());
    }

    @Test
    void waitsOnlyInsideWindowForTheSameRow() {
        RetryBackoff backoff = new RetryBackoff(1000, 8000);
        UUID id = UUID.randomUUID();

        assertFalse(backoff.mustWait(id, t0));                       // nothing failed yet
        backoff.recordFailure(id, t0);
        assertTrue(backoff.mustWait(id, t0.plusMillis(500)));
        assertFalse(backoff.mustWait(id, t0.plusMillis(1000)));
    }

    @Test
    void differentHeadRowStartsFromScratch() {
        RetryBackoff backoff = new RetryBackoff(1000, 8000);
        backoff.recordFailure(UUID.randomUUID(), t0);
        backoff.recordFailure(backoff.attempts() > 0 ? UUID.randomUUID() : null, t0);

        UUID other = UUID.randomUUID();
        assertFalse(backoff.mustWait(other, t0));                    // resets
        assertEquals(1000, backoff.recordFailure(other, t0));        // back to the initial delay
    }
}
