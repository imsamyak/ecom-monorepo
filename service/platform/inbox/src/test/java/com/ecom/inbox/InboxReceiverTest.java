package com.ecom.inbox;

import com.ecom.contract.EventEnvelope;
import com.ecom.contract.event.ProductEvent;
import com.ecom.contract.event.VariantEvent;
import com.ecom.inbox.port.spi.InboxStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@org.springframework.context.annotation.Import(InboxReceiverTest.TestListener.class)
public class InboxReceiverTest {

    @Autowired
    private InboxReceiver inboxReceiver;

    @Autowired
    private InboxStore inboxStore;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestListener testListener;

    @BeforeEach
    void setUp() {
        // Clear the received events before each test
        testListener.clear();
    }

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestListener {
        private final List<Object> receivedEvents = new ArrayList<>();
        private boolean throwError = false;

        @EventListener
        public void onProductEvent(ProductEvent event) {
            // Record the parent interface event
            receivedEvents.add(event);
        }

        @EventListener
        public void onProductCreated(ProductEvent.CREATE event) {
            // Record the specific record event
            receivedEvents.add(event);
            if (throwError) {
                // Simulate a failure during event processing
                throw new RuntimeException("Simulated listener failure");
            }
        }

        public void clear() {
            // Reset state
            receivedEvents.clear();
            throwError = false;
        }

        public void setThrowError(boolean throwError) {
            // Configure whether to throw an error
            this.throwError = throwError;
        }

        public List<Object> getReceivedEvents() {
            // Return collected events
            return receivedEvents;
        }
    }

    @Test
    void aNewMessageReachesTheRecordListenerAndTheSealedParentListener() throws Exception {
        // Setup a new CREATE event message
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        ProductEvent.CREATE createEvent = new ProductEvent.CREATE(UUID.fromString(productId), UUID.randomUUID(), "Test Product", "Description", 10.0, java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), "ACTIVE");
        
        EventEnvelope envelope = new EventEnvelope("Product", "CREATE", objectMapper.valueToTree(createEvent));
        String payload = objectMapper.writeValueAsString(envelope);
        InboxMessage message = new InboxMessage(eventId, "Product", productId, payload);
        
        // Deliver the message for the first time
        inboxReceiver.receive(message);
        
        // Verify it reaches both the record type listener and the sealed parent listener
        assertThat(testListener.getReceivedEvents()).hasSize(2);
        
        // Verify the event is now stored as a duplicate
        assertThat(inboxStore.isDuplicate(eventId, "Product", productId)).isTrue();
    }

    @Test
    void theSameMessageAgainIsSkipped() throws Exception {
        // Setup a new CREATE event message
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        ProductEvent.CREATE createEvent = new ProductEvent.CREATE(UUID.fromString(productId), UUID.randomUUID(), "Test Product", "Description", 10.0, java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), "ACTIVE");
        
        EventEnvelope envelope = new EventEnvelope("Product", "CREATE", objectMapper.valueToTree(createEvent));
        String payload = objectMapper.writeValueAsString(envelope);
        InboxMessage message = new InboxMessage(eventId, "Product", productId, payload);
        
        // Deliver the message for the first time
        inboxReceiver.receive(message);
        
        // Clear the listener state for the duplicate test
        testListener.clear();
        
        // Deliver the same message again
        inboxReceiver.receive(message);
        
        // Verify no listener runs because it was skipped
        assertThat(testListener.getReceivedEvents()).isEmpty();
    }

    @Test
    void aFailingListenerRollsBackAndTheExceptionPropagates() throws Exception {
        // Setup a message that will cause the listener to throw
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        ProductEvent.CREATE createEvent = new ProductEvent.CREATE(UUID.fromString(productId), UUID.randomUUID(), "Test Product", "Description", 10.0, java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), "ACTIVE");
        
        EventEnvelope envelope = new EventEnvelope("Product", "CREATE", objectMapper.valueToTree(createEvent));
        String payload = objectMapper.writeValueAsString(envelope);
        InboxMessage message = new InboxMessage(eventId, "Product", productId, payload);
        
        // Configure the listener to throw
        testListener.setThrowError(true);
        
        // Verify the exception propagates out of the receiver
        assertThrows(RuntimeException.class, () -> inboxReceiver.receive(message));
        
        // Verify the store is rolled back (the duplicate check is false)
        assertThat(inboxStore.isDuplicate(eventId, "Product", productId)).isFalse();
    }

    @Test
    void afterADeliveryWhoseListenerThrowsDeliveringTheSameMessageAgainIsProcessed() throws Exception {
        // Setup a message that will cause the listener to throw
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        ProductEvent.CREATE createEvent = new ProductEvent.CREATE(UUID.fromString(productId), UUID.randomUUID(), "Test Product", "Description", 10.0, java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), "ACTIVE");
        
        EventEnvelope envelope = new EventEnvelope("Product", "CREATE", objectMapper.valueToTree(createEvent));
        String payload = objectMapper.writeValueAsString(envelope);
        InboxMessage message = new InboxMessage(eventId, "Product", productId, payload);
        
        // Configure the listener to throw on the first delivery
        testListener.setThrowError(true);
        
        // Deliver the message and expect it to fail
        assertThrows(RuntimeException.class, () -> inboxReceiver.receive(message));
        
        // Configure the listener to process successfully on the second delivery
        testListener.clear();
        
        // Deliver the exact same message again
        inboxReceiver.receive(message);
        
        // Verify it reaches the listeners on the second try
        assertThat(testListener.getReceivedEvents()).hasSize(2);
        
        // Verify the event is now stored as a duplicate
        assertThat(inboxStore.isDuplicate(eventId, "Product", productId)).isTrue();
    }

    @Test
    void anEventWithNoListenerIsStoredAndAcknowledged() throws Exception {
        // Setup an ADD event message for which there is no listener
        String eventId = UUID.randomUUID().toString();
        String variantId = UUID.randomUUID().toString();
        VariantEvent.ADD addEvent = new VariantEvent.ADD(1L, UUID.fromString(variantId), java.util.Map.of("color", "red"), java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        
        EventEnvelope envelope = new EventEnvelope("Variant", "ADD", objectMapper.valueToTree(addEvent));
        String payload = objectMapper.writeValueAsString(envelope);
        InboxMessage message = new InboxMessage(eventId, "Variant", variantId, payload);
        
        // Deliver the message
        inboxReceiver.receive(message);
        
        // Verify nothing was received by our test listener
        assertThat(testListener.getReceivedEvents()).isEmpty();
        
        // Verify the event is acknowledged (stored as duplicate)
        assertThat(inboxStore.isDuplicate(eventId, "Variant", variantId)).isTrue();
    }

    @Test
    void anUnknownActionIsNotDispatchedButStoredAndAcknowledged() throws Exception {
        // Setup a message with an unknown action
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        EventEnvelope envelope = new EventEnvelope("Product", "UNKNOWN_ACTION", objectMapper.createObjectNode());
        String payload = objectMapper.writeValueAsString(envelope);
        InboxMessage message = new InboxMessage(eventId, "Product", productId, payload);
        
        // Deliver the message
        inboxReceiver.receive(message);
        
        // Verify it was not dispatched to listeners
        assertThat(testListener.getReceivedEvents()).isEmpty();
        
        // Verify the event is acknowledged (stored as duplicate)
        assertThat(inboxStore.isDuplicate(eventId, "Product", productId)).isTrue();
    }

    @Test
    void anUnknownAggregateIsNotDispatchedButStoredAndAcknowledged() throws Exception {
        // Setup a message with an unknown aggregate
        String eventId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        EventEnvelope envelope = new EventEnvelope("Order", "CREATE", objectMapper.createObjectNode());
        String payload = objectMapper.writeValueAsString(envelope);
        InboxMessage message = new InboxMessage(eventId, "Order", orderId, payload);
        
        // Deliver the message
        inboxReceiver.receive(message);
        
        // Verify it was not dispatched to listeners
        assertThat(testListener.getReceivedEvents()).isEmpty();
        
        // Verify the event is acknowledged (stored as duplicate)
        assertThat(inboxStore.isDuplicate(eventId, "Order", orderId)).isTrue();
    }
}
