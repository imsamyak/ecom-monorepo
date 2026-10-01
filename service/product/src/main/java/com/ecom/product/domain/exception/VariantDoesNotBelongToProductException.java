package com.ecom.product.domain.exception;

import com.ecom.shared.exception.InvalidRequestException;
import java.util.UUID;

public class VariantDoesNotBelongToProductException extends InvalidRequestException {

    public VariantDoesNotBelongToProductException(UUID expectedProductId, Long variantId) {
        super(String.format("Variant with ID %d does not belong to the specified product with ID %s", variantId, expectedProductId));
    }
}
