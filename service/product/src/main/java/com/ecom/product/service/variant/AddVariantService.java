package com.ecom.product.service.variant;

import com.ecom.product.domain.entity.Product;
import com.ecom.product.domain.entity.Variant;
import com.ecom.product.port.in.usecase.variant.AddVariantUseCase;
import com.ecom.product.port.in.usecase.variant.dto.command.AddVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.product.port.out.persistence.variant.SaveVariantPort;
import com.ecom.product.service.variant.mapper.VariantMapper;
import com.ecom.product.domain.exception.ProductNotFoundException;
import com.ecom.product.domain.exception.ProductNotOwnedException;

import com.ecom.contract.DomainEvent;
// Nested event record imported directly because the Variant entity is already imported in this class
import com.ecom.contract.event.VariantEvent.ADD;
import lombok.RequiredArgsConstructor;

import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

@Service
@Validated
@RequiredArgsConstructor
public class AddVariantService implements AddVariantUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveVariantPort saveVariantPort;
    private final VariantMapper variantMapper;

    @Override
    @Transactional
    public VariantResult execute(AddVariantCommand command) {

        Product product = loadProductPort.loadProduct(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        if (!product.getSellerId().equals(command.sellerId())) {
            throw new ProductNotOwnedException(command.productId(), command.sellerId());
        }

        Variant variant = Variant.builder()
                .product(product)
                .properties(new TreeMap<>(command.properties()))
                .build();

        Variant savedVariant = saveVariantPort.saveVariant(variant);

        return variantMapper.toResult(savedVariant);
    }

    @Override
    public DomainEvent buildEvent(AddVariantCommand command, VariantResult result) {
        // Build the ADD event using the mapper
        return variantMapper.toAddEvent(result);
    }
}
