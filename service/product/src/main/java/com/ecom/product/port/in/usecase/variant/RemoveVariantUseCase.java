package com.ecom.product.port.in.usecase.variant;

import jakarta.validation.Valid;

import com.ecom.product.port.in.usecase.variant.dto.command.RemoveVariantCommand;

public interface RemoveVariantUseCase {
    void removeVariant(@Valid RemoveVariantCommand command);
}


