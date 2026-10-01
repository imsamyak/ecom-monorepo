package com.ecom.inbox.config;

import com.ecom.inbox.InboxTestApplication;
import com.ecom.inbox.cleaner.InboxCleaner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class InboxAutoConfigurationTest {

    @Nested
    @SpringBootTest(classes = InboxTestApplication.class)
    class DefaultTtlTest {

        @Autowired
        private InboxCleaner inboxCleaner;

        @Test
        void withNoPropertyTheAutoConfiguredCleanerUses7Days() {
            // Assert: the default TTL is 7 days
            assertThat(inboxCleaner.getTtl()).isEqualTo(Duration.ofDays(7));
        }
    }

    @Nested
    @SpringBootTest(classes = InboxTestApplication.class, properties = "inbox.dedupe.ttl=2d")
    class ConfiguredTtlTest {

        @Autowired
        private InboxCleaner inboxCleaner;

        @Test
        void withPropertyItUsesTheConfiguredTtl() {
            // Assert: the TTL matches the configured property
            assertThat(inboxCleaner.getTtl()).isEqualTo(Duration.ofDays(2));
        }
    }
}
