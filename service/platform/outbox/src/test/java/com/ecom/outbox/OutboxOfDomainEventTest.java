package com.ecom.outbox;

import com.ecom.contract.DomainEvent;
import com.ecom.contract.EventEnvelope;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// A record that is not nested in an aggregate interface has no aggregate type, so it must be rejected
record TopLevelEvent(String id) implements DomainEvent {
    @Override
    public String aggregateId() {
        return id;
    }
}

class OutboxOfDomainEventTest {

    // Sample aggregate: the interface name is the aggregate type, each nested record is an action
    sealed interface Order extends DomainEvent {
        UUID orderId();

        @Override
        default String aggregateId() {
            return orderId().toString();
        }

        record Placed(UUID orderId, double total) implements Order {}
    }

    // Aggregate whose id is built by the record itself from a number
    sealed interface Invoice extends DomainEvent {
        record Issued(long number) implements Invoice {
            @Override
            public String aggregateId() {
                return String.valueOf(number);
            }
        }
    }

    // Aggregate whose id is a composite string
    sealed interface Shipment extends DomainEvent {
        record Sent(UUID customerId, String region) implements Shipment {
            @Override
            public String aggregateId() {
                return customerId + ":" + region;
            }
        }
    }

    // Aggregates whose id is missing or blank
    sealed interface Broken extends DomainEvent {
        record NoId() implements Broken {
            @Override
            public String aggregateId() {
                return null;
            }
        }

        record BlankId() implements Broken {
            @Override
            public String aggregateId() {
                return "   ";
            }
        }
    }

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aggregateTypeIsTheNameOfTheInterfaceTheRecordIsNestedIn() {
        // Build an outbox from a nested event record
        Outbox outbox = Outbox.of(new Order.Placed(UUID.randomUUID(), 5.0));

        // The enclosing interface name is the aggregate type
        assertEquals("Order", outbox.getAggregateType());
    }

    @Test
    void actionIsTheRecordNameAndIsCarriedInTheEnvelope() {
        // Build an outbox from a nested event record
        Order.Placed event = new Order.Placed(UUID.randomUUID(), 5.0);
        Outbox outbox = Outbox.of(event);

        // The payload is an envelope: aggregate, action, and the event itself as data
        EventEnvelope envelope = assertInstanceOf(EventEnvelope.class, outbox.getPayload());
        assertEquals("Order", envelope.aggregate());
        assertEquals("Placed", envelope.action());
        assertSame(event, envelope.data());
    }

    @Test
    void aggregateIdIsExactlyWhatTheEventReturns() {
        // The event default method turns a UUID into its string form
        UUID id = UUID.randomUUID();
        assertEquals(id.toString(), Outbox.of(new Order.Placed(id, 1.0)).getAggregateId());

        // A record can convert a number itself
        assertEquals("1007", Outbox.of(new Invoice.Issued(1007L)).getAggregateId());

        // A record can build a composite key
        UUID customer = UUID.randomUUID();
        assertEquals(customer + ":EU", Outbox.of(new Shipment.Sent(customer, "EU")).getAggregateId());
    }

    @Test
    void resultPassesTheOutboxValidationRules() {
        // Build from a valid event
        Outbox outbox = Outbox.of(new Order.Placed(UUID.randomUUID(), 1.0));

        // Type, id and payload all satisfy the constraints the aspect enforces
        assertTrue(validator.validate(outbox).isEmpty());
    }

    @Test
    void nullAggregateIdIsRejectedWithAMessageNamingTheEvent() {
        // An event that returns no id cannot be partitioned
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Outbox.of(new Broken.NoId()));

        // The message tells the developer which event is wrong
        assertTrue(ex.getMessage().contains("Broken"), ex.getMessage());
    }

    @Test
    void blankAggregateIdIsRejectedWithAMessageNamingTheEvent() {
        // A blank id is as useless as a missing one
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Outbox.of(new Broken.BlankId()));

        // The message names the event
        assertTrue(ex.getMessage().contains("Broken"), ex.getMessage());
    }

    @Test
    void aTopLevelRecordHasNoAggregateTypeAndIsRejected() {
        // A record outside any aggregate interface cannot say what it belongs to
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Outbox.of(new TopLevelEvent("1")));

        // The message names the offending class
        assertTrue(ex.getMessage().contains("TopLevelEvent"), ex.getMessage());
    }

    @Test
    void anAnonymousEventIsRejected() {
        // An anonymous class has no usable name
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
    void aLambdaEventIsRejected() {
        // A lambda is a synthetic class whose name is generated
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
