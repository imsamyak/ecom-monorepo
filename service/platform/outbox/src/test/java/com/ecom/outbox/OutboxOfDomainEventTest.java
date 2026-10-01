package com.ecom.outbox;

import com.ecom.confess.DomainEvent;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxOfDomainEventTest {

    // Sample events: the record name is the aggregate type, the chosen attribute is the aggregate id
    private record Order(UUID id, double total) implements DomainEvent {
        @Override
        public String aggregateId() {
            return id.toString();
        }
    }

    private record Invoice(long number) implements DomainEvent {
        @Override
        public String aggregateId() {
            return String.valueOf(number);
        }
    }

    private record Coupon(String code) implements DomainEvent {
        @Override
        public String aggregateId() {
            return code;
        }
    }

    private record Shipment(UUID customerId, String region) implements DomainEvent {
        // A composite key built from two attributes, already a string
        @Override
        public String aggregateId() {
            return customerId + ":" + region;
        }
    }

    private record NoId() implements DomainEvent {
        @Override
        public String aggregateId() {
            return null;
        }
    }

    private record BlankId() implements DomainEvent {
        @Override
        public String aggregateId() {
            return "   ";
        }
    }

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aggregateTypeIsTheSimpleNameOfTheEventRecord() {
        // Build an outbox from an event record
        Outbox outbox = Outbox.of(new Order(UUID.randomUUID(), 5.0));

        // No configuration needed: the record's own name is the type
        assertEquals("Order", outbox.getAggregateType());
    }

    @Test
    void uuidAggregateIdBecomesItsStringForm() {
        // The record chose a UUID attribute
        UUID id = UUID.randomUUID();

        // The record converted it, and the outbox uses that string unchanged
        assertEquals(id.toString(), Outbox.of(new Order(id, 1.0)).getAggregateId());
    }

    @Test
    void numericAggregateIdBecomesItsStringForm() {
        // The record turned its number into a string, and the outbox uses it unchanged
        assertEquals("1007", Outbox.of(new Invoice(1007L)).getAggregateId());
    }

    @Test
    void stringAggregateIdIsKeptAsIs() {
        // The record chose a string attribute
        assertEquals("SPRING-10", Outbox.of(new Coupon("SPRING-10")).getAggregateId());
    }

    @Test
    void aCompositeKeyBuiltByTheRecordIsUsedAsIs() {
        // The record combined two attributes into one key
        UUID customer = UUID.randomUUID();

        // Whatever the record returns is what gets used
        assertEquals(customer + ":EU", Outbox.of(new Shipment(customer, "EU")).getAggregateId());
    }

    @Test
    void payloadIsTheEventItself() {
        // Build from an event
        Order event = new Order(UUID.randomUUID(), 9.5);

        // The same instance is carried as payload, so the aspect serializes the record's components
        assertSame(event, Outbox.of(event).getPayload());
    }

    @Test
    void resultPassesTheOutboxValidationRules() {
        // Build from a valid event
        Outbox outbox = Outbox.of(new Order(UUID.randomUUID(), 1.0));

        // Type, id and payload all satisfy the constraints the aspect enforces
        assertTrue(validator.validate(outbox).isEmpty());
    }

    @Test
    void nullAggregateIdIsRejectedWithAMessageNamingTheEvent() {
        // An event that returns no id cannot be partitioned
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Outbox.of(new NoId()));

        // The message tells the developer which event is wrong
        assertTrue(ex.getMessage().contains("NoId"), ex.getMessage());
    }

    @Test
    void blankAggregateIdIsRejectedWithAMessageNamingTheEvent() {
        // A blank id is as useless as a missing one
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Outbox.of(new BlankId()));

        // The message names the event
        assertTrue(ex.getMessage().contains("BlankId"), ex.getMessage());
    }

    @Test
    void anAnonymousEventHasNoUsableTypeNameAndIsRejected() {
        // An anonymous class has an empty simple name
        DomainEvent anonymous = new DomainEvent() {
            @Override
            public String aggregateId() {
                return "x";
            }
        };

        // Refuse it instead of storing an empty aggregate type
        assertThrows(IllegalArgumentException.class, () -> Outbox.of(anonymous));
    }

    @Test
    void aLambdaEventHasNoUsableTypeNameAndIsRejected() {
        // A lambda is a synthetic class whose name is generated, not a real event name
        DomainEvent lambda = () -> "x";

        // Refuse it instead of storing a generated class name as the aggregate type
        assertThrows(IllegalArgumentException.class, () -> Outbox.of(lambda));
    }

    @Test
    void aNullEventIsRejected() {
        // There is nothing to build from
        assertThrows(NullPointerException.class, () -> Outbox.of(null));
    }
}
