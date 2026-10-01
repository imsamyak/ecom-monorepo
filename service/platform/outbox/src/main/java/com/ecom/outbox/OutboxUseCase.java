package com.ecom.outbox;

import com.ecom.contract.DomainEvent;
import com.ecom.shared.usecase.UseCase;

/**
 * A {@link UseCase} that emits an event through the outbox. The outbox row is written in the same transaction as
 * {@link #execute}. Return {@code null} from {@link #buildEvent} to skip emitting. The use case only describes what
 * happened; the aggregate type, action, envelope and JSON are produced by the outbox module.
 */
public interface OutboxUseCase<C, R> extends UseCase<C, R> {

    DomainEvent buildEvent(C command, R result);
}
