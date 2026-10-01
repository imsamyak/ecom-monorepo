package com.ecom.product.adapter.out.persistence;

import com.ecom.product.adapter.out.persistence.repository.VariantRepository;
import com.ecom.product.domain.entity.Variant;
import com.ecom.product.port.out.persistence.variant.DeleteVariantPort;
import com.ecom.product.port.out.persistence.variant.LoadVariantPort;
import com.ecom.product.port.out.persistence.variant.SaveVariantPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VariantPersistenceAdapter implements SaveVariantPort, LoadVariantPort, DeleteVariantPort {

    private final VariantRepository variantRepository;

    @Override
    public Variant saveVariant(Variant variant) {
        return variantRepository.save(variant);
    }

    @Override
    public Optional<Variant> loadVariant(Long variantId) {
        return variantRepository.findById(variantId);
    }

    @Override
    public void deleteVariant(Long variantId) {
        variantRepository.deleteById(variantId);
    }
}
