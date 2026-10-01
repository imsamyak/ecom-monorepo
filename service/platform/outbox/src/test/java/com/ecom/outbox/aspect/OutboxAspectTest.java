package com.ecom.outbox.aspect;

import com.ecom.contract.DomainEvent;
import com.ecom.outbox.OutboxUseCase;
import com.ecom.outbox.entity.OutboxEntity;
import com.ecom.outbox.enums.OutboxStatus;
import com.ecom.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OutboxAspectTest {

    // Sample aggregate and action used as the emitted event
    sealed interface Thing extends DomainEvent {
        record Made(UUID thingId, String name) implements Thing {
            @Override
            public String aggregateId() {
                return thingId.toString();
            }
        }

        // An event whose id is blank, which the outbox must refuse
        record Blank() implements Thing {
            @Override
            public String aggregateId() {
                return "   ";
            }
        }
    }

    // Use case under test: the event and result are supplied by the test through a Supplier
    static class SampleUseCase implements OutboxUseCase<String, String> {
        Supplier<DomainEvent> eventSupplier = () -> null;
        RuntimeException failure;
        Exception checkedFailure;

        @Override
        public String execute(String command) {
            if (failure != null) {
                throw failure;
            }
            if (checkedFailure != null) {
                // Sneaky throw to simulate a checked exception escaping a use case
                SampleUseCase.<RuntimeException>sneaky(checkedFailure);
            }
            return "result:" + command;
        }

        @Override
        public DomainEvent buildEvent(String command, String result) {
            return eventSupplier.get();
        }

        @SuppressWarnings("unchecked")
        private static <T extends Throwable> void sneaky(Throwable t) throws T {
            throw (T) t;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private OutboxRepository repository;
    private PlatformTransactionManager transactionManager;
    private SampleUseCase target;
    private OutboxUseCase<String, String> proxy;

    // Builds the aspect with the given payload limit and returns a proxied use case
    private OutboxUseCase<String, String> proxyWithLimit(int maxPayloadLength) {
        OutboxAspect aspect = new OutboxAspect(repository, mapper,
                Validation.buildDefaultValidatorFactory().getValidator(), maxPayloadLength,
                new TransactionTemplate(transactionManager));
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    @BeforeEach
    void setUp() {
        // Mocked collaborators: nothing touches a real database
        repository = mock(OutboxRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        target = new SampleUseCase();
        proxy = proxyWithLimit(10_000);
    }

    private OutboxEntity savedRow() {
        ArgumentCaptor<OutboxEntity> captor = ArgumentCaptor.forClass(OutboxEntity.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void returnsTheUseCaseResultUnchanged() {
        // The use case emits no event
        assertEquals("result:x", proxy.execute("x"));
    }

    @Test
    void savesOneRowWithAggregateTypeIdAndEnvelopePayload() throws IOException {
        // The use case emits one event
        UUID id = UUID.randomUUID();
        target.eventSupplier = () -> new Thing.Made(id, "widget");

        // Run it through the aspect
        proxy.execute("x");

        // One pending row is saved with type, id and the envelope as JSON
        OutboxEntity row = savedRow();
        assertNotNull(row.getId());
        assertEquals("Thing", row.getAggregateType());
        assertEquals(id.toString(), row.getAggregateId());
        assertEquals(OutboxStatus.PENDING, row.getStatus());
        JsonNode json = mapper.readTree(row.getPayload());
        assertEquals("Thing", json.get("aggregate").asText());
        assertEquals("Made", json.get("action").asText());
        assertEquals("widget", json.get("data").get("name").asText());
        assertEquals(id.toString(), json.get("data").get("thingId").asText());
        assertEquals(3, json.size());
    }

    @Test
    void savesNothingWhenTheUseCaseReturnsNoEvent() {
        // The use case decides there is nothing to publish
        target.eventSupplier = () -> null;

        // Run it
        proxy.execute("x");

        // No row is written
        verify(repository, never()).save(any());
    }

    @Test
    void failsTheUseCaseWhenThePayloadIsOverTheConfiguredLimit() {
        // A very small limit
        proxy = proxyWithLimit(20);
        target.eventSupplier = () -> new Thing.Made(UUID.randomUUID(), "widget");

        // The use case fails with a message naming the property, and nothing is saved
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> proxy.execute("x"));
        assertTrue(ex.getMessage().contains("outbox.max-payload-length"), ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void acceptsAPayloadExactlyAtTheLimit() throws IOException {
        // Find the exact payload length first
        UUID id = UUID.randomUUID();
        target.eventSupplier = () -> new Thing.Made(id, "widget");
        proxy.execute("x");
        int exact = savedRow().getPayload().length();

        // A limit equal to that length still passes
        repository = mock(OutboxRepository.class);
        proxy = proxyWithLimit(exact);
        proxy.execute("x");
        assertEquals(exact, savedRow().getPayload().length());
    }

    @Test
    void anInvalidEventFailsTheUseCaseAndSavesNothing() {
        // An event with a blank id
        target.eventSupplier = Thing.Blank::new;

        // The use case fails and no row is written
        assertThrows(IllegalArgumentException.class, () -> proxy.execute("x"));
        verify(repository, never()).save(any());
    }

    @Test
    void aRuntimeExceptionFromTheUseCaseSavesNothingRollsBackAndPropagates() {
        // The use case itself fails
        target.failure = new IllegalStateException("boom");
        target.eventSupplier = () -> new Thing.Made(UUID.randomUUID(), "widget");

        // The same exception reaches the caller, nothing is saved, and the transaction is rolled back
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> proxy.execute("x"));
        assertEquals("boom", ex.getMessage());
        verify(repository, never()).save(any());
        verify(transactionManager).rollback(any());
    }

    @Test
    void aCheckedExceptionFromTheUseCaseIsNotSwallowedAndRollsBack() {
        // The use case throws a checked exception
        target.checkedFailure = new IOException("disk");

        // The aspect rethrows the original exception; the JDK proxy may wrap an undeclared checked exception,
        // so look through that wrapper
        Throwable thrown = assertThrows(Throwable.class, () -> proxy.execute("x"));
        Throwable root = thrown instanceof UndeclaredThrowableException u ? u.getUndeclaredThrowable() : thrown;
        assertInstanceOf(IOException.class, root);

        // Nothing is saved and the transaction is rolled back
        verify(repository, never()).save(any());
        verify(transactionManager).rollback(any());
    }

    @Test
    void commitsTheTransactionWhenEverythingSucceeds() {
        // The use case emits one event
        target.eventSupplier = () -> new Thing.Made(UUID.randomUUID(), "widget");

        // Run it
        proxy.execute("x");

        // The business work and the outbox row were committed together
        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }
}
