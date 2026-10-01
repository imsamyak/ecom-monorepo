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
import com.ecom.outbox.Outbox;

@Service
@Validated
@RequiredArgsConstructor
public class CreateProductService implements CreateProductUseCase {

    private final SaveProductPort saveProductPort;
    private final ProductMapper productMapper;

    @Override
    public ProductResult execute(CreateProductCommand command) {
        Product product = Product.builder()
                .sellerId(command.sellerId())
                .title(command.title())
                .description(command.description())
                .price(command.price())
                .build();

        Product savedProduct = saveProductPort.saveProduct(product);

        return productMapper.toResult(savedProduct);
    }

    @Override
    public Outbox buildOutbox(CreateProductCommand command, ProductResult result) {
        return Outbox.builder()
                .aggregateType("Product")
                .aggregateId(String.valueOf(result.id()))
                .payload(result)
                .build();
    }
}
