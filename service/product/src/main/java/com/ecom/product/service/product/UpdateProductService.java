package com.ecom.product.service.product;

import com.ecom.product.domain.exception.ProductNotFoundException;

import com.ecom.product.port.in.usecase.product.UpdateProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.product.port.out.persistence.product.SaveProductPort;
import com.ecom.contract.DomainEvent;
// Nested event record imported directly because the Product entity is already imported in this class
import com.ecom.contract.event.ProductEvent.UPDATE;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ecom.product.service.product.mapper.ProductMapper;
import org.springframework.validation.annotation.Validated;
import com.ecom.product.domain.entity.Product;

import com.ecom.shared.exception.InvalidRequestException;

@Service
@Validated
@RequiredArgsConstructor
public class UpdateProductService implements UpdateProductUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final ProductMapper productMapper;

    @Override
    public ProductResult execute(UpdateProductCommand command) {
        // empty body check
        if (command.title() == null && command.description() == null && command.price() == null && command.status() == null) {
            throw new InvalidRequestException("Empty request body");
        }

        // null checks for title, price, status
        if (command.title() != null && command.title().isEmpty()) {
            throw new InvalidRequestException("Title cannot be null");
        }
        if (command.price() != null && command.price().isEmpty()) {
            throw new InvalidRequestException("Price cannot be null");
        }
        if (command.status() != null && command.status().isEmpty()) {
            throw new InvalidRequestException("Status cannot be null");
        }

        // Load the product or throw an exception if it does not exist
        Product product = loadProductPort.loadProduct(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // Reject the request if the seller does not own this product
        product.verifyOwnership(command.sellerId());

        // Update the product properties if present
        if (command.title() != null) {
            product.setTitle(command.title().get());
        }
        if (command.description() != null) {
            product.setDescription(command.description().orElse(null));
        }
        if (command.price() != null) {
            product.setPrice(command.price().get());
        }
        if (command.status() != null) {
            if (command.status().get() == com.ecom.product.domain.enums.ProductStatus.ACTIVE) {
                product.activate();
            } else {
                product.deactivate();
            }
        }

        // Save the updated product
        Product savedProduct = saveProductPort.saveProduct(product);

        // Map the saved product to a result
        return productMapper.toResult(savedProduct);
    }

    @Override
    public DomainEvent buildEvent(UpdateProductCommand command, ProductResult result) {
        // Build the UPDATE event using the mapper
        return productMapper.toUpdateEvent(result);
    }
}
