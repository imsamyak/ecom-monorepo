package com.ecom.contract;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EventEnvelopeTest {

    @Test
    void hasExactlyAggregateActionAndDataInThatOrder() {
        // The envelope shape is part of the contract with consumers, so pin its fields and their order
        List<String> names = Arrays.stream(EventEnvelope.class.getRecordComponents())
                .map(RecordComponent::getName).toList();

        assertEquals(List.of("aggregate", "action", "data"), names);
    }

    @Test
    void carriesTheGivenValuesUnchanged() {
        // Build an envelope around some data
        Object data = new Object();
        EventEnvelope envelope = new EventEnvelope("Product", "Created", data);

        // Nothing is copied or altered
        assertEquals("Product", envelope.aggregate());
        assertEquals("Created", envelope.action());
        assertSame(data, envelope.data());
    }
}
