package com.ecom.outbox.relay;

import com.ecom.outbox.OutboxMessage;
import com.ecom.outbox.entity.OutboxEntity;
import com.ecom.outbox.enums.OutboxStatus;
import com.ecom.outbox.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Publishes pending rows strictly in creation order, one row per transaction (a crash can duplicate at most
 * the single event in flight, and a DB connection is held only while that one event is published).
 * The first failure stops the run: the failing row and everything after it stay PENDING and are retried from the
 * same row on later polls, so events are never published out of order. A row that can never be published
 * therefore blocks the outbox until it is fixed; watch the error log. Retries back off exponentially
 * (initial, 2x, 4x, ... capped); that state is kept in memory, so a restart retries again from the initial delay.
 * Each published row is deleted right after its transaction commits, by an async {@link OutboxCleaner} run.
 * Run it on one instance only ({@code outbox.relay.enabled}).
 */
@Slf4j
public class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final ObjectProvider<OutboxPublisher> publisherProvider;
    private final TransactionTemplate transactionTemplate;
    private final int maxEventsPerRun;
    private final OutboxCleaner cleaner;
    private final RetryBackoff backoff;

    public OutboxRelay(OutboxRepository outboxRepository, ObjectProvider<OutboxPublisher> publisherProvider,
                       TransactionTemplate transactionTemplate, OutboxCleaner cleaner, int maxEventsPerRun,
                       long initialBackoffMs, long maxBackoffMs) {
        this.outboxRepository = outboxRepository;
        this.publisherProvider = publisherProvider;
        this.transactionTemplate = transactionTemplate;
        this.cleaner = cleaner;
        this.maxEventsPerRun = maxEventsPerRun;
        this.backoff = new RetryBackoff(initialBackoffMs, maxBackoffMs);
    }

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:1000}")
    public void processOutboxEvents() {
        OutboxPublisher publisher = publisherProvider.getIfAvailable();
        if (publisher == null) {
            return;
        }
        for (int i = 0; i < maxEventsPerRun; i++) {
            if (!Boolean.TRUE.equals(transactionTemplate.execute(status -> publishNext(publisher)))) {
                return;
            }
            cleaner.cleanup();   // after commit; async, so a slow or failed delete never touches publishing
        }
    }

    /** Publishes the oldest pending row. Returns true if it was published and there may be more. */
    private boolean publishNext(OutboxPublisher publisher) {
        List<OutboxEntity> head = outboxRepository.lockPendingBatch(OutboxStatus.PENDING, PageRequest.of(0, 1));
        if (head.isEmpty()) {
            return false;
        }
        OutboxEntity entity = head.get(0);
        if (backoff.mustWait(entity.getId(), Instant.now())) {
            return false;
        }

        try {
            publisher.publish(new OutboxMessage(entity.getId(), entity.getAggregateType(), entity.getAggregateId(), entity.getPayload()));
        } catch (Exception e) {
            long delayMs = backoff.recordFailure(entity.getId(), Instant.now());
            log.error("Failed to publish outbox event {} ({}), attempt {}; stopping so order is preserved, retry in {} ms",
                    entity.getId(), entity.getAggregateType(), backoff.attempts(), delayMs, e);
            return false;
        }

        entity.setStatus(OutboxStatus.PROCESSED);
        entity.setProcessedAt(LocalDateTime.now(ZoneOffset.UTC));
        log.debug("Published outbox event {} ({})", entity.getId(), entity.getAggregateType());
        return true;
    }
}
