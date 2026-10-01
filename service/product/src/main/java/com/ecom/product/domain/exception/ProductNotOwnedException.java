package com.ecom.product.domain.exception;

import java.util.UUID;
import com.ecom.shared.exception.UnauthorizedAccessException;

public class ProductNotOwnedException extends UnauthorizedAccessException {
    public ProductNotOwnedException(UUID productId, UUID sellerId) {
        super(String.format("Seller '%s' is not authorized to modify Product '%s'", sellerId, productId));
    }
}
