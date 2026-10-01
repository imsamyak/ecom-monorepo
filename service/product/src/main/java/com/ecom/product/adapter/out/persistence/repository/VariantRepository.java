package com.ecom.product.adapter.out.persistence.repository;

import com.ecom.product.domain.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VariantRepository extends JpaRepository<Variant, Long> {

    List<Variant> findByProductId(UUID productId);

}
