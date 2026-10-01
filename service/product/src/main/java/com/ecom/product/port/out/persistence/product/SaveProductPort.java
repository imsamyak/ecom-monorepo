package com.ecom.product.port.out.persistence.product;

import com.ecom.product.domain.entity.Product;

public interface SaveProductPort {
    Product saveProduct(Product product);
}
