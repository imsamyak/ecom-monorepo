package com.ecom.product.port.in.usecase.product;

import com.ecom.outbox.OutboxAwareUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;

// A delete has no result value, hence Void
public interface DeleteProductUseCase extends OutboxAwareUseCase<DeleteProductCommand, Void> {

}
