package com.ecom.product.port.in.usecase.variant.dto.result;

import lombok.Builder;
import java.util.Map;
import java.util.UUID;

import java.time.LocalDateTime;

@Builder
public record VariantResult(
    Long id,
    UUID productId,
    Map<String, String> properties,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

