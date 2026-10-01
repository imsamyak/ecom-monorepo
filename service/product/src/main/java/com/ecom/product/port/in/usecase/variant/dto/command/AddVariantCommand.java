package com.ecom.product.port.in.usecase.variant.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record AddVariantCommand(
    @NotNull(message = "Product ID cannot be null") UUID productId,
    @NotNull(message = "Seller ID cannot be null") UUID sellerId,
    @NotEmpty(message = "Variant properties cannot be empty")
    Map<@NotBlank(message = "Property key cannot be blank") String, @NotBlank(message = "Property value cannot be blank") String> properties
) {}
