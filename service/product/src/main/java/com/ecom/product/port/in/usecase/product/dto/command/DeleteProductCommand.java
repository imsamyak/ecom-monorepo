package com.ecom.product.port.in.usecase.product.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record DeleteProductCommand(
    @NotNull(message = "Product ID cannot be null") UUID productId,
    @NotNull(message = "Seller ID cannot be null") UUID sellerId
) {}
