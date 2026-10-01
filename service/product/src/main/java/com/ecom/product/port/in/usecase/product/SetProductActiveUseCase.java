package com.ecom.product.port.in.usecase.product;

import com.ecom.product.port.in.usecase.product.dto.command.SetProductActiveCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import jakarta.validation.Valid;

public interface SetProductActiveUseCase {
    ProductResult execute(@Valid SetProductActiveCommand command);
}
