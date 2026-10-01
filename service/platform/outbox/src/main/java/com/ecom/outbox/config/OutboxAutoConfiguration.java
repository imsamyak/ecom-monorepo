package com.ecom.outbox.config;

import com.ecom.outbox.OutboxAwareUseCase;
import com.ecom.outbox.aspect.OutboxAspect;
import com.ecom.outbox.relay.OutboxCleaner;
import com.ecom.outbox.relay.OutboxPublisher;
import com.ecom.outbox.relay.OutboxRelay;
import com.ecom.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Plug-and-play wiring: adding the outbox dependency is enough. The package is added to the auto-configuration
 * packages (additive, unlike @EntityScan / @EnableJpaRepositories which would replace the application's own scan).
 */
@AutoConfiguration(
        after = {JacksonAutoConfiguration.class, ValidationAutoConfiguration.class},
        before = {JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigurationPackage(basePackageClasses = OutboxAwareUseCase.class)
@EnableScheduling
@EnableAsync
public class OutboxAutoConfiguration {

    @Bean
    public OutboxAspect outboxAspect(OutboxRepository outboxRepository, ObjectMapper objectMapper,
                                     Validator validator, PlatformTransactionManager transactionManager,
                                     @Value("${outbox.max-payload-length:1000000}") int maxPayloadLength) {
        return new OutboxAspect(outboxRepository, objectMapper, validator, maxPayloadLength, new TransactionTemplate(transactionManager));
    }

    /** Disable with outbox.relay.enabled=false on every instance except the one that should publish. */
    @Bean
    @ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
    public OutboxRelay outboxRelay(OutboxRepository outboxRepository, ObjectProvider<OutboxPublisher> publisher,
                                   PlatformTransactionManager transactionManager,
                                   OutboxCleaner outboxCleaner,
                                   @Value("${outbox.relay.batch-size:50}") int batchSize,
                                   @Value("${outbox.relay.initial-backoff-ms:1000}") long initialBackoffMs,
                                   @Value("${outbox.relay.max-backoff-ms:300000}") long maxBackoffMs) {
        return new OutboxRelay(outboxRepository, publisher, new TransactionTemplate(transactionManager), outboxCleaner, batchSize,
                initialBackoffMs, maxBackoffMs);
    }

    @Bean
    @ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
    public OutboxCleaner outboxCleaner(OutboxRepository outboxRepository) {
        return new OutboxCleaner(outboxRepository);
    }

    /** One thread: cleanups never overlap. A cleanup queued behind a running one is dropped; the running one already covers it. */
    @Bean(name = "outboxCleanupExecutor")
    @ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolTaskExecutor outboxCleanupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("outbox-cleanup-");
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }
}
