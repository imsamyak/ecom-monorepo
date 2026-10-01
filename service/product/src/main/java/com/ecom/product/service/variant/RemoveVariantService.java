package com.ecom.product.service.variant;

import com.ecom.product.domain.entity.Variant;
import com.ecom.product.port.in.usecase.variant.RemoveVariantUseCase;
import com.ecom.product.port.in.usecase.variant.dto.command.RemoveVariantCommand;
import com.ecom.product.port.out.persistence.variant.DeleteVariantPort;
import com.ecom.product.port.out.persistence.variant.LoadVariantPort;
import com.ecom.product.domain.exception.VariantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

@Service
@Validated
@RequiredArgsConstructor
public class RemoveVariantService implements RemoveVariantUseCase {

    private final LoadVariantPort loadVariantPort;
    private final DeleteVariantPort deleteVariantPort;

    @Override
    @Transactional
    public void removeVariant(RemoveVariantCommand command) {
        Variant variant = loadVariantPort.loadVariant(command.variantId())
                .orElseThrow(() -> new VariantNotFoundException(command.variantId()));

        variant.verifyBelongsToProduct(command.productId());

        variant.getProduct().verifyOwnership(command.sellerId());

        deleteVariantPort.deleteVariant(command.variantId());
    }
}
