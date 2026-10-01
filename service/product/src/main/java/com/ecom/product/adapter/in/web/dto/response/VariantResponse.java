package com.ecom.product.adapter.in.web.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record VariantResponse(
    Long id,
    UUID productId,
    Map<String, String> properties,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

