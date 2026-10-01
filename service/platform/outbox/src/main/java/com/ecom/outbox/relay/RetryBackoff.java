package com.ecom.outbox.relay;

import java.time.Instant;
import java.util.UUID;

/**
 * In-memory exponential backoff for the one row the relay is currently stuck on (1s, 2s, 4s, ... capped).
 * Not persisted on purpose: after a restart the retry simply starts again from the initial delay.
 */
class RetryBackoff {

    private final long initialMs;
    private final long maxMs;

    private UUID failedId;
    private int attempts;
    private Instant nextAttemptAt;

    RetryBackoff(long initialMs, long maxMs) {
        this.initialMs = initialMs;
        this.maxMs = maxMs;
    }

    /** True while {@code headId} (the oldest pending row) is still inside its backoff window. */
    boolean mustWait(UUID headId, Instant now) {
        if (!headId.equals(failedId)) {
            reset();   // the row we were stuck on is gone (published elsewhere); start fresh
            return false;
        }
        return now.isBefore(nextAttemptAt);
    }

    /** Records a failure of {@code id} and returns the delay before the next attempt, in ms. */
    long recordFailure(UUID id, Instant now) {
        attempts = id.equals(failedId) ? attempts + 1 : 1;
        failedId = id;
        long delayMs = Math.min(initialMs * (1L << Math.min(attempts - 1, 30)), maxMs);
        nextAttemptAt = now.plusMillis(delayMs);
        return delayMs;
    }

    int attempts() {
        return attempts;
    }

    void reset() {
        failedId = null;
        attempts = 0;
        nextAttemptAt = null;
    }
}
