package com.ecom.outbox.aspect;

import com.ecom.contract.DomainEvent;
import com.ecom.outbox.Outbox;
import com.ecom.outbox.OutboxUseCase;
import com.ecom.outbox.entity.OutboxEntity;
import com.ecom.outbox.repository.OutboxRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Set;
import java.util.UUID;

/**
 * Wraps every {@link OutboxUseCase#execute} in a transaction and writes the outbox row in that same
 * transaction, so the business change and its event commit or roll back together. If the caller already
 * has a transaction the aspect joins it.
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class OutboxAspect {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    /** From {@code outbox.max-payload-length}: serialized JSON length limit, in characters. */
    private final int maxPayloadLength;
    private final TransactionTemplate transactionTemplate;

    @Around("execution(* com.ecom.outbox.OutboxUseCase+.execute(..))")
    public Object interceptUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    return proceedAndRecord(joinPoint);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable t) {
                    throw new UndeclaredThrowableException(t);
                }
            });
        } catch (UndeclaredThrowableException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("unchecked")
    private Object proceedAndRecord(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        Object command = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;
        OutboxUseCase<Object, Object> useCase = (OutboxUseCase<Object, Object>) joinPoint.getTarget();
        // The use case only describes what happened; everything else is derived from the event
        DomainEvent event = useCase.buildEvent(command, result);

        if (event != null) {
            Outbox outbox = Outbox.of(event);
            validate(outbox);
            String payload = objectMapper.writeValueAsString(outbox.getPayload());
            if (payload.length() > maxPayloadLength) {
                throw new IllegalStateException("Outbox payload is " + payload.length()
                        + " characters, exceeds outbox.max-payload-length (" + maxPayloadLength + ") in " + useCase.getClass().getName());
            }
            OutboxEntity entity = OutboxEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(outbox.getAggregateType())
                    .aggregateId(outbox.getAggregateId())
                    .payload(payload)
                    .build();
            validate(entity);
            outboxRepository.save(entity);
            log.debug("Saved outbox event: {}", outbox.getAggregateType());
        }
        return result;
    }

    private <T> void validate(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
