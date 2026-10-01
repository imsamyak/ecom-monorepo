package com.ecom.product.port.in.usecase.product;

import com.ecom.outbox.OutboxUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;

public interface UpdateProductUseCase extends OutboxUseCase<UpdateProductCommand, ProductResult> {

}
