package com.ecom.contract;

/**
 * The JSON shape of every outbox payload: which aggregate the event is about, what happened to it, and the event data.
 * Consumers parse this first, then read {@code data} according to the aggregate and action.
 */
public record EventEnvelope(String aggregate, String action, Object data) {
}
