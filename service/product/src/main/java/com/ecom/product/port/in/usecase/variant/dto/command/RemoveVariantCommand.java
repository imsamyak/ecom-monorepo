package com.ecom.product.port.in.usecase.variant.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RemoveVariantCommand(
    @NotNull(message = "Product ID cannot be null") UUID productId,
    @NotNull(message = "Variant ID cannot be null") Long variantId,
    @NotNull(message = "Seller ID cannot be null") UUID sellerId
) {}
