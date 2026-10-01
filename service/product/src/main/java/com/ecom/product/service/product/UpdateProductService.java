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

@Service
@Validated
@RequiredArgsConstructor
public class UpdateProductService implements UpdateProductUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final ProductMapper productMapper;

    @Override
    public ProductResult execute(UpdateProductCommand command) {
        // Load the product or throw an exception if it does not exist
        Product product = loadProductPort.loadProduct(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // Reject the request if the seller does not own this product
        product.verifyOwnership(command.sellerId());

        // Update the product properties
        product.setTitle(command.title());
        product.setDescription(command.description());
        product.setPrice(command.price());

        // Save the updated product
        Product savedProduct = saveProductPort.saveProduct(product);

        // Map the saved product to a result
        return productMapper.toResult(savedProduct);
    }

    @Override
    public DomainEvent buildEvent(UpdateProductCommand command, ProductResult result) {
        // Describe the updated product as an UPDATE event carrying the snapshot after the change
        return new UPDATE(
                result.id(),
                result.sellerId(),
                result.title(),
                result.description(),
                result.price(),
                result.createdAt(),
                result.updatedAt(),
                result.status().name());
    }
}
