package com.ecom.product.domain.event;

import com.ecom.confess.DomainEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class VariantEventTest {

    // A fully populated event used by every test
    private final UUID productId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
    private final Variant event = new Variant(7L, productId, Map.of("color", "red"), now, now);

    @Test
    void isADomainEvent() {
        // The record plugs into the outbox through the shared contract
        assertInstanceOf(DomainEvent.class, event);
    }

    @Test
    void theOwningProductIdIsTheAggregateIdSoVariantEventsStayOrderedWithTheirProduct() {
        // Variant events share the product's partition key, so they are consumed in order per product
        assertEquals(productId.toString(), event.aggregateId());
    }

    @Test
    void theRecordNameIsTheAggregateType() {
        // The type comes from the class name
        assertEquals("Variant", event.getClass().getSimpleName());
    }

    @Test
    void serializesToTheSameJsonFieldsAsAVariantResult() throws Exception {
        // Serialize the way the outbox aspect does
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        JsonNode json = mapper.readTree(mapper.writeValueAsString(event));

        // Exactly these fields, and the DomainEvent method is not leaked into the payload
        Set<String> fields = StreamSupport.stream(((Iterable<String>) json::fieldNames).spliterator(), false)
                .collect(Collectors.toSet());
        assertEquals(Set.of("id", "productId", "properties", "createdAt", "updatedAt"), fields);
        assertEquals("red", json.get("properties").get("color").asText());
    }
}
