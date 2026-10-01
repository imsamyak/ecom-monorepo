package com.ecom.product.adapter.in.web.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    UUID sellerId,
    String title,
    String description,
    double price,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean active
) {}

