package com.ecom.product;

import com.ecom.outbox.OutboxMessage;
import com.ecom.outbox.entity.OutboxEntity;
import com.ecom.outbox.enums.OutboxStatus;
import com.ecom.outbox.relay.OutboxCleaner;
import com.ecom.outbox.relay.OutboxPublisher;
import com.ecom.outbox.relay.OutboxRelay;
import com.ecom.outbox.repository.OutboxRepository;
import com.ecom.product.port.in.usecase.product.CreateProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "outbox.relay.fixed-delay-ms=3600000",   // the test drives the relay by hand
        "outbox.relay.initial-backoff-ms=400",
        "outbox.relay.max-backoff-ms=400",
        "outbox.max-payload-length=400"
})
@Import(OutboxIntegrationTest.TestPublisherConfig.class)
class OutboxIntegrationTest {

    static final List<OutboxMessage> PUBLISHED = new CopyOnWriteArrayList<>();
    static final AtomicBoolean BROKER_DOWN = new AtomicBoolean(false);

    @TestConfiguration
    static class TestPublisherConfig {
        @Bean
        OutboxPublisher testPublisher() {
            return message -> {
                if (BROKER_DOWN.get()) {
                    throw new IllegalStateException("broker down");
                }
                PUBLISHED.add(message);
            };
        }
    }

    @Autowired CreateProductUseCase createProduct;
    @Autowired OutboxRepository outboxRepository;
    @Autowired OutboxRelay relay;
    @Autowired OutboxCleaner cleaner;

    @BeforeEach
    void reset() {
        outboxRepository.deleteAll();
        PUBLISHED.clear();
        BROKER_DOWN.set(false);
    }

    private ProductResult create(String title) {
        return createProduct.execute(new CreateProductCommand(UUID.randomUUID(), title, "desc", 10.0));
    }

    @Test
    void writesOutboxRowWithUseCaseAndRelaysIt() {
        ProductResult result = create("Phone");

        List<OutboxEntity> rows = outboxRepository.findAll();
        assertEquals(1, rows.size());
        assertEquals("Product", rows.get(0).getAggregateType());
        assertEquals(result.id().toString(), rows.get(0).getAggregateId());
        assertEquals(OutboxStatus.PENDING, rows.get(0).getStatus());

        relay.processOutboxEvents();

        assertEquals(1, PUBLISHED.size());
        assertTrue(PUBLISHED.get(0).payload().contains("Phone"));
        assertEquals(result.id().toString(), PUBLISHED.get(0).aggregateId());
        // the published row is deleted asynchronously right after the publish
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertEquals(0, outboxRepository.count()));
    }

    @Test
    void failureBlocksLaterEventsThenRetriesWithBackoffInOrder() throws InterruptedException {
        create("First");
        create("Second");

        BROKER_DOWN.set(true);
        relay.processOutboxEvents();
        assertEquals(0, PUBLISHED.size());
        assertEquals(2, outboxRepository.findAll().stream().filter(e -> e.getStatus() == OutboxStatus.PENDING).count());

        // still inside the backoff window, broker back up: nothing is retried yet
        BROKER_DOWN.set(false);
        relay.processOutboxEvents();
        assertEquals(0, PUBLISHED.size());

        Thread.sleep(500);
        relay.processOutboxEvents();
        assertEquals(2, PUBLISHED.size());
        assertTrue(PUBLISHED.get(0).payload().contains("First"));
        assertTrue(PUBLISHED.get(1).payload().contains("Second"));
    }

    @Test
    void sweepRemovesLeftoverProcessedRowsButNotPending() {
        outboxRepository.save(processedRow(LocalDateTime.now(ZoneOffset.UTC)));
        create("Pending stays");

        cleaner.purge();

        List<OutboxEntity> rows = outboxRepository.findAll();
        assertEquals(1, rows.size());
        assertEquals(OutboxStatus.PENDING, rows.get(0).getStatus());
    }

    @Test
    void cleanupDeletesOnlyProcessedRowsAndKeepsPending() {
        create("First");
        create("Second");
        BROKER_DOWN.set(false);
        relay.processOutboxEvents();   // publishes both, each followed by an async cleanup
        create("Third");               // stays pending: the relay is not run again

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            List<OutboxEntity> rows = outboxRepository.findAll();
            assertEquals(1, rows.size());
            assertEquals(OutboxStatus.PENDING, rows.get(0).getStatus());
        });
    }

    private static OutboxEntity processedRow(LocalDateTime processedAt) {
        return OutboxEntity.builder().id(UUID.randomUUID()).aggregateType("Product").payload("{}")
                .status(OutboxStatus.PROCESSED).processedAt(processedAt).build();
    }

    @Test
    void payloadOverConfiguredLimitFailsTheUseCase() {
        assertThrows(IllegalStateException.class, () -> createProduct.execute(
                new CreateProductCommand(UUID.randomUUID(), "Big", "d".repeat(450), 10.0)));

        assertEquals(0, outboxRepository.count());
    }

    @Test
    void failedUseCaseLeavesNoOutboxRow() {
        assertThrows(Exception.class, () -> createProduct.execute(
                new CreateProductCommand(UUID.randomUUID(), "x", "desc", -1)));

        assertEquals(0, outboxRepository.count());
    }
}
