package com.ecom.product.port.in.usecase.product.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Optional;
import java.util.UUID;
import com.ecom.product.domain.enums.ProductStatus;

@Builder
public record UpdateProductCommand(
    @NotNull(message = "Product ID cannot be null") UUID productId,
    @NotNull(message = "Seller ID cannot be null") UUID sellerId,
    Optional<@Size(min = 3, max = 100, message = "Product title must be between 3 and 100 characters") String> title,
    Optional<@Size(max = 500, message = "Product description cannot exceed 500 characters") String> description,
    Optional<@Positive(message = "Product price must be greater than zero") Double> price,
    Optional<ProductStatus> status
) {}
