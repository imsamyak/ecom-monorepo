package com.ecom.product.adapter.out.persistence.repository;

import com.ecom.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findBySellerId(UUID sellerId);

}
