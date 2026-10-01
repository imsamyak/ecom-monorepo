package com.ecom.product.port.in.usecase.product.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateProductCommand(
    @NotNull(message = "Seller ID cannot be null") UUID sellerId,
    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 100, message = "Product title must be between 3 and 100 characters") String title,
    @Size(max = 500, message = "Product description cannot exceed 500 characters") String description,
    @Positive(message = "Product price must be greater than zero") double price
) {}
