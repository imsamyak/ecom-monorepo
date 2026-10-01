package com.ecom.product.port.in.usecase.product;

import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;

public interface UpdateProductUseCase {
    ProductResult updateProduct(UpdateProductCommand command);
}


