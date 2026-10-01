package com.ecom.inbox.cleaner;

import com.ecom.inbox.adapter.out.persistence.InboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

public class InboxCleaner {

    private static final Logger log = LoggerFactory.getLogger(InboxCleaner.class);

    private final InboxRepository inboxRepository;
    private Duration ttl;

    public InboxCleaner(InboxRepository inboxRepository, Duration ttl) {
        this.inboxRepository = inboxRepository;
        this.ttl = ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

    // Run hourly to clean up old deduplication rows
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanup() {
        // Calculate the threshold based on the TTL
        Instant threshold = Instant.now().minus(ttl);
        
        // Delete rows older than the threshold
        int deleted = inboxRepository.deleteOlderThan(threshold);
        
        if (deleted > 0) {
            log.info("Deleted {} expired inbox deduplication rows older than {}", deleted, threshold);
        }
    }
}
