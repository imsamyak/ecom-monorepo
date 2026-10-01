package com.ecom.product.port.in.usecase.variant;

import com.ecom.outbox.OutboxAwareUseCase;
import com.ecom.product.port.in.usecase.variant.dto.command.RemoveVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;

// The result is the data of the variant that was removed, so the Removed event can carry it
public interface RemoveVariantUseCase extends OutboxAwareUseCase<RemoveVariantCommand, VariantResult> {

}
