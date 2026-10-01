package com.ecom.product.service.product;

import com.ecom.product.port.in.usecase.product.CreateProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.out.persistence.product.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.ecom.product.domain.entity.Product;
import com.ecom.product.service.product.mapper.ProductMapper;
import com.ecom.contract.DomainEvent;
// Nested event record imported directly because the Product entity is already imported in this class
import com.ecom.contract.event.ProductEvent.CREATE;

@Service
@Validated
@RequiredArgsConstructor
public class CreateProductService implements CreateProductUseCase {

    private final SaveProductPort saveProductPort;
    private final ProductMapper productMapper;

    @Override
    public ProductResult execute(CreateProductCommand command) {
        // Build the new product from the command
        Product product = Product.builder()
                .sellerId(command.sellerId())
                .title(command.title())
                .description(command.description())
                .price(command.price())
                .build();

        // Save the product to the database
        Product savedProduct = saveProductPort.saveProduct(product);

        // Map the saved product to a result
        return productMapper.toResult(savedProduct);
    }

    @Override
    public DomainEvent buildEvent(CreateProductCommand command, ProductResult result) {
        // Describe the created product as a CREATE event; the outbox derives type, action and id from it
        return new CREATE(
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
