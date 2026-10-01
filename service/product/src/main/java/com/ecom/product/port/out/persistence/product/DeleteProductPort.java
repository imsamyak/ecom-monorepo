package com.ecom.product.port.out.persistence.product;

import java.util.UUID;

public interface DeleteProductPort {
    void deleteProduct(UUID productId);
}
