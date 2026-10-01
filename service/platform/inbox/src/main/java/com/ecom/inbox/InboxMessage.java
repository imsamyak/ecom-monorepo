package com.ecom.inbox;

public record InboxMessage(String eventId, String aggregateType, String aggregateId, String payload) {
}
