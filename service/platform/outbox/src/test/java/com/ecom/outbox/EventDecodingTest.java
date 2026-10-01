package com.ecom.outbox;

import com.ecom.contract.DomainEvent;
import com.ecom.contract.EventCatalog;
import com.ecom.contract.EventEnvelope;
import com.ecom.contract.event.ProductEvent;
import com.ecom.contract.event.VariantEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventDecodingTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void roundTripProductCreate() throws Exception {
        // Build a complete Product CREATE event to test its specific components
        ProductEvent.CREATE event = new ProductEvent.CREATE(
                UUID.randomUUID(), UUID.randomUUID(), "Title", "Desc", 10.0,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "ACTIVE"
        );
        
        // Verify it can be safely round-tripped through JSON
        assertRoundTrip(event);
    }

    @Test
    void roundTripProductUpdate() throws Exception {
        // Build a complete Product UPDATE event
        ProductEvent.UPDATE event = new ProductEvent.UPDATE(
                UUID.randomUUID(), UUID.randomUUID(), "Title", "Desc", 10.0,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "INACTIVE"
        );
        
        // Verify it can be safely round-tripped through JSON
        assertRoundTrip(event);
    }

    @Test
    void roundTripProductDelete() throws Exception {
        // Build a Product DELETE event carrying only identifiers
        ProductEvent.DELETE event = new ProductEvent.DELETE(UUID.randomUUID(), UUID.randomUUID());
        
        // Verify it can be safely round-tripped through JSON
        assertRoundTrip(event);
    }

    @Test
    void roundTripVariantAdd() throws Exception {
        // Build a Variant ADD event carrying the full property map
        VariantEvent.ADD event = new VariantEvent.ADD(
                1L, UUID.randomUUID(), Map.of("color", "red"),
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
        
        // Verify it can be safely round-tripped through JSON
        assertRoundTrip(event);
    }

    @Test
    void roundTripVariantRemove() throws Exception {
        // Build a Variant REMOVE event
        VariantEvent.REMOVE event = new VariantEvent.REMOVE(1L, UUID.randomUUID(), Map.of("color", "red"));
        
        // Verify it can be safely round-tripped through JSON
        assertRoundTrip(event);
    }

    private void assertRoundTrip(DomainEvent event) throws Exception {
        // Build the envelope from the event exactly as the outbox aspect does
        Outbox outbox = Outbox.of(event);
        EventEnvelope originalEnvelope = (EventEnvelope) outbox.getPayload();

        // Serialize the envelope to JSON to simulate database storage
        String json = objectMapper.writeValueAsString(originalEnvelope);

        // Deserialize the envelope from JSON to simulate the inbox receiver reading it
        EventEnvelope parsedEnvelope = objectMapper.readValue(json, EventEnvelope.class);

        // Resolve the specific record class from the envelope's aggregate and action types
        Class<? extends DomainEvent> recordClass = EventCatalog.resolve(
                parsedEnvelope.aggregate(), parsedEnvelope.action()
        ).orElseThrow(() -> new AssertionError("Record class not found in catalog"));

        // Convert the envelope's raw data object into the resolved record type
        DomainEvent decoded = objectMapper.convertValue(parsedEnvelope.data(), recordClass);

        // Verify the decoded record matches the original event exactly
        assertEquals(event, decoded);
    }
}
