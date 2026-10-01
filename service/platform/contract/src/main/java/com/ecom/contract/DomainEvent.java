package com.ecom.contract;

/**
 * Contract for events published through the outbox. The implementing record's own simple name is the aggregate type
 * (for example a record named {@code Product} describes the Product aggregate), so only the id has to be supplied.
 */
public interface DomainEvent {

    // The record picks the attribute that identifies the aggregate and returns it already converted to a String
    String aggregateId();
}
