package com.ecom.product.domain.exception;

import com.ecom.shared.exception.ResourceNotFoundException;

public class VariantNotFoundException extends ResourceNotFoundException {
    public VariantNotFoundException(Long variantId) {
        super(String.format("Variant with ID '%s' was not found", variantId));
    }
}
