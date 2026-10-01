package com.ecom.outbox;

import com.ecom.shared.usecase.UseCase;

/**
 * A {@link UseCase} that emits an outbox event. The outbox row is written in the same transaction as
 * {@link #execute}. Return {@code null} from {@link #buildOutbox} to skip emitting.
 */
public interface OutboxUseCase<C, R> extends UseCase<C, R> {

    Outbox buildOutbox(C command, R result);
}
