package com.ecom.outbox.relay;

import com.ecom.outbox.enums.OutboxStatus;
import com.ecom.outbox.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Deletes PROCESSED rows older than the retention period so the table does not grow forever. */
@Slf4j
public class OutboxCleaner {

    private final OutboxRepository outboxRepository;
    private final Duration retention;

    public OutboxCleaner(OutboxRepository outboxRepository, Duration retention) {
        this.outboxRepository = outboxRepository;
        this.retention = retention;
    }

    @Scheduled(cron = "${outbox.cleanup.cron:0 0 * * * *}")
    @Transactional
    public void purge() {
        int deleted = outboxRepository.deleteProcessedBefore(OutboxStatus.PROCESSED, LocalDateTime.now(ZoneOffset.UTC).minus(retention));
        if (deleted > 0) {
            log.info("Purged {} processed outbox events older than {}", deleted, retention);
        }
    }
}
