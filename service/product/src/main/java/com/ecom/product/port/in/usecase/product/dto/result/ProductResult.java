package com.ecom.product.port.in.usecase.product.dto.result;

import lombok.Builder;
import java.util.UUID;

import java.time.LocalDateTime;

@Builder
public record ProductResult(
    UUID id,
    UUID sellerId,
    String title,
    String description,
    double price,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean active
) {}

