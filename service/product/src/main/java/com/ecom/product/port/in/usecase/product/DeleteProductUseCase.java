package com.ecom.product.port.in.usecase.product;

import jakarta.validation.Valid;

import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;

public interface DeleteProductUseCase {
    void deleteProduct(@Valid DeleteProductCommand command);
}


