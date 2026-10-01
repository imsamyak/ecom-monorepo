package com.ecom.product.port.out.persistence.variant;

import com.ecom.product.domain.entity.Variant;
import java.util.Optional;

public interface LoadVariantPort {
    Optional<Variant> loadVariant(Long variantId);
}
