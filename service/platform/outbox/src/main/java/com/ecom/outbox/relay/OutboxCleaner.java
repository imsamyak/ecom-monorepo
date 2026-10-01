package com.ecom.outbox.relay;

import com.ecom.outbox.enums.OutboxStatus;
import com.ecom.outbox.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Deletes PROCESSED rows. {@link #cleanup()} runs asynchronously after every publish, so a failed delete can never
 * affect publishing and the next publish retries it. {@link #purge()} is an hourly sweep for rows left behind
 * (a lost or failed cleanup, or no publish after the last row).
 */
@Slf4j
public class OutboxCleaner {

    private final OutboxRepository outboxRepository;
    private final AtomicBoolean running = new AtomicBoolean();

    public OutboxCleaner(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    /** Skipped if a cleanup is already in flight: it removes every PROCESSED row, and the next publish triggers another. */
    @Async("outboxCleanupExecutor")
    public void cleanup() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            outboxRepository.deleteByStatus(OutboxStatus.PROCESSED);
        } catch (Exception e) {
            log.warn("Outbox cleanup failed; the next publish or the hourly sweep will retry", e);
        } finally {
            running.set(false);
        }
    }

    @Scheduled(cron = "${outbox.cleanup.cron:0 0 * * * *}")
    @Transactional
    public void purge() {
        int deleted = outboxRepository.deleteByStatus(OutboxStatus.PROCESSED);
        if (deleted > 0) {
            log.info("Sweep removed {} leftover processed outbox events", deleted);
        }
    }
}
