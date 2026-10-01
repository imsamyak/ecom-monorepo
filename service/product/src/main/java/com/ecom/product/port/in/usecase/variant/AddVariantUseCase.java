package com.ecom.product.port.in.usecase.variant;

import com.ecom.product.port.in.usecase.variant.dto.command.AddVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;

public interface AddVariantUseCase {
    VariantResult addVariant(AddVariantCommand command);
}


