package com.ecom.product.port.in.usecase.product;

import com.ecom.outbox.OutboxUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;

// A delete has no result value, hence Void
public interface DeleteProductUseCase extends OutboxUseCase<DeleteProductCommand, Void> {

}
