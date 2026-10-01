package com.ecom.product.port.in.usecase.variant;

import com.ecom.outbox.OutboxUseCase;
import com.ecom.product.port.in.usecase.variant.dto.command.AddVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;

public interface AddVariantUseCase extends OutboxUseCase<AddVariantCommand, VariantResult> {

}
