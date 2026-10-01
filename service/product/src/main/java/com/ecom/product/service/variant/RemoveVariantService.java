package com.ecom.product.service.variant;

import com.ecom.contract.DomainEvent;
// Nested event record imported directly because the Variant entity is already imported in this class
import com.ecom.contract.event.Variant.Removed;
import com.ecom.product.domain.entity.Variant;
import com.ecom.product.domain.exception.VariantNotFoundException;
import com.ecom.product.port.in.usecase.variant.RemoveVariantUseCase;
import com.ecom.product.port.in.usecase.variant.dto.command.RemoveVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import com.ecom.product.port.out.persistence.variant.DeleteVariantPort;
import com.ecom.product.port.out.persistence.variant.LoadVariantPort;
import com.ecom.product.service.variant.mapper.VariantMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor
public class RemoveVariantService implements RemoveVariantUseCase {

    private final LoadVariantPort loadVariantPort;
    private final DeleteVariantPort deleteVariantPort;
    private final VariantMapper variantMapper;
    private final Validator validator;

    @Override
    @Transactional
    public VariantResult execute(RemoveVariantCommand command) {
        // Reject an invalid command; this was validated before the method was renamed to execute
        Set<ConstraintViolation<RemoveVariantCommand>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Load the variant, failing if it does not exist
        Variant variant = loadVariantPort.loadVariant(command.variantId())
                .orElseThrow(() -> new VariantNotFoundException(command.variantId()));

        // The variant must belong to the product named in the request
        variant.verifyBelongsToProduct(command.productId());

        // The seller must own that product
        variant.getProduct().verifyOwnership(command.sellerId());

        // Capture the variant's data before it is deleted so the Removed event can carry it
        VariantResult removed = variantMapper.toResult(variant);

        // Delete the variant
        deleteVariantPort.deleteVariant(command.variantId());

        return removed;
    }

    @Override
    public DomainEvent buildEvent(RemoveVariantCommand command, VariantResult result) {
        // Describe what was removed; the aggregate id is the owning product id
        return new Removed(result.id(), result.productId(), result.properties());
    }
}
