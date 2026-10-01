package com.ecom.product.adapter.out.repository;

import com.ecom.product.domain.entity.Variant;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VariantRepository extends Repository<Variant, Long> {

    Variant save(Variant variant);

    Optional<Variant> findById(Long id);

    List<Variant> findByProductId(UUID productId);

    void delete(Variant variant);

    void deleteById(Long id);
}
