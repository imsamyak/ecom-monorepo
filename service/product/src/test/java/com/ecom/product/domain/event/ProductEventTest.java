package com.ecom.product.domain.event;

import com.ecom.confess.DomainEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ProductEventTest {

    // A fully populated event used by every test
    private final UUID id = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
    private final Product event = new Product(id, sellerId, "Phone", "desc", 10.5, now, now);

    @Test
    void isADomainEvent() {
        // The record plugs into the outbox through the shared contract
        assertInstanceOf(DomainEvent.class, event);
    }

    @Test
    void theProductIdIsTheAggregateId() {
        // The event is about one product, so its id is the partition key
        assertEquals(id, event.aggregateId());
    }

    @Test
    void theRecordNameIsTheAggregateType() {
        // The type comes from the class name
        assertEquals("Product", event.getClass().getSimpleName());
    }

    @Test
    void serializesToTheSameJsonFieldsTheOutboxPayloadAlwaysHad() throws Exception {
        // Serialize the way the outbox aspect does
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        JsonNode json = mapper.readTree(mapper.writeValueAsString(event));

        // Consumers of existing events must see exactly these fields; the DomainEvent method is not leaked
        Set<String> fields = StreamSupport.stream(((Iterable<String>) json::fieldNames).spliterator(), false)
                .collect(Collectors.toSet());
        assertEquals(Set.of("id", "sellerId", "title", "description", "price", "createdAt", "updatedAt"), fields);
        assertEquals("Phone", json.get("title").asText());
        assertEquals(id.toString(), json.get("id").asText());
    }
}
