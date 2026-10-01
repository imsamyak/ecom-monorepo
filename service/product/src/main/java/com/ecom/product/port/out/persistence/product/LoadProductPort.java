package com.ecom.product.port.out.persistence.product;

import com.ecom.product.domain.entity.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadProductPort {
    Optional<Product> loadProduct(UUID productId);
    List<Product> loadProductsBySeller(UUID sellerId);
}
