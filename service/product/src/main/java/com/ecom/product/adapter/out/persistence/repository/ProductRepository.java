package com.ecom.product.adapter.out.repository;

import com.ecom.product.domain.entity.Product;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends Repository<Product, UUID> {

    Product save(Product product);
    
    Optional<Product> findById(UUID id);

    List<Product> findBySellerId(UUID sellerId);

    void delete(Product product);

    void deleteById(UUID id);
}
