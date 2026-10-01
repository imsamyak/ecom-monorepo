package com.ecom.product.port.in.usecase.product;

import com.ecom.outbox.OutboxAwareUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;

public interface UpdateProductUseCase extends OutboxAwareUseCase<UpdateProductCommand, ProductResult> {

}
