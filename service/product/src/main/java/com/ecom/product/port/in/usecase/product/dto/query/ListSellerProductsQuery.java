package com.ecom.product.port.in.usecase.product.dto.query;

import lombok.Builder;
import java.util.UUID;

@Builder
public record ListSellerProductsQuery(
    UUID sellerId
) {}

