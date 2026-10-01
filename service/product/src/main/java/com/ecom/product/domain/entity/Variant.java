package com.ecom.product.domain.entity;

import com.ecom.product.domain.converter.VariantSkuConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import lombok.*;

import java.util.*;
import com.ecom.shared.entity.DomainEntity;
import com.ecom.product.domain.exception.VariantDoesNotBelongToProductException;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "sku_uc", columnNames = { "sku", "product_id" }))
public class Variant extends DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    // The sku is built from the properties; the database refuses an empty or whitespace-only one
    @Builder.Default
    @Convert(converter = VariantSkuConverter.class)
    @Column(name = "sku", nullable = false)
    @Check(constraints = "LENGTH(TRIM(sku)) > 0")
    private final TreeMap<String, String> properties = new TreeMap<>();

    public void verifyBelongsToProduct(UUID expectedProductId) {
        if (!this.product.getId().equals(expectedProductId)) {
            throw new VariantDoesNotBelongToProductException(expectedProductId, this.id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Variant variant))
            return false;

        return Objects.equals(this.properties, variant.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.properties);
    }
}
