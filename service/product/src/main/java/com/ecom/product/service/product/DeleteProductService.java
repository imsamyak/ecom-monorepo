package com.ecom.product.service.product;

import com.ecom.product.domain.exception.ProductNotFoundException;

import com.ecom.product.port.in.usecase.product.DeleteProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;
import com.ecom.product.port.out.persistence.product.DeleteProductPort;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.contract.DomainEvent;
// Nested event record imported directly because the Product entity is already imported in this class
import com.ecom.contract.event.Product.DELETE;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import org.springframework.validation.annotation.Validated;
import com.ecom.product.domain.entity.Product;

@Service
@Validated
@RequiredArgsConstructor
public class DeleteProductService implements DeleteProductUseCase {

    private final LoadProductPort loadProductPort;
    private final DeleteProductPort deleteProductPort;
    private final Validator validator;

    @Override
    public Void execute(DeleteProductCommand command) {
        // Reject an invalid command; this was validated before the method was renamed to execute
        Set<ConstraintViolation<DeleteProductCommand>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Product product = loadProductPort.loadProduct(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.verifyOwnership(command.sellerId());

        deleteProductPort.deleteProduct(command.productId());

        // There is nothing to return for a delete
        return null;
    }


    @Override
    public DomainEvent buildEvent(DeleteProductCommand command, Void result) {
        // A delete has no result, so the DELETE event is built from the command alone
        return new DELETE(command.productId(), command.sellerId());
    }
}
