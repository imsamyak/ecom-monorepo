package com.ecom.inbox.config;

import com.ecom.inbox.adapter.out.persistence.InboxJpaAdapter;
import com.ecom.inbox.adapter.out.persistence.InboxRepository;
import com.ecom.inbox.cleaner.InboxCleaner;
import com.ecom.inbox.port.spi.InboxStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

@AutoConfiguration(before = {JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@AutoConfigurationPackage(basePackages = "com.ecom.inbox")
@EnableScheduling
public class InboxAutoConfiguration {

    @Bean
    public InboxStore inboxStore(InboxRepository inboxRepository) {
        return new InboxJpaAdapter(inboxRepository);
    }

    @Bean
    public InboxCleaner inboxCleaner(InboxRepository inboxRepository,
                                     @Value("${inbox.dedupe.ttl:7d}") Duration ttl) {
        return new InboxCleaner(inboxRepository, ttl);
    }
}
