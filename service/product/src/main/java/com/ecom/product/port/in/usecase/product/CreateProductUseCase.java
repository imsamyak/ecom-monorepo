package com.ecom.product.port.in.usecase.product;

import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.outbox.OutboxUseCase;

public interface CreateProductUseCase extends OutboxUseCase<CreateProductCommand, ProductResult> {
    
}


