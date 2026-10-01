package com.ecom.product.domain.exception;

import java.util.UUID;
import com.ecom.shared.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(UUID productId) {
        super(String.format("Product with ID '%s' was not found", productId));
    }
}
