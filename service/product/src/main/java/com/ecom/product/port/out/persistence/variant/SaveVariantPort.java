package com.ecom.product.port.out.persistence.variant;

import com.ecom.product.domain.entity.Variant;

public interface SaveVariantPort {
    Variant saveVariant(Variant variant);
}
