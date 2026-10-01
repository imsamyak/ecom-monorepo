package com.ecom.product.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.ecom.product.domain.exception.ProductNotOwnedException;
import com.ecom.shared.entity.DomainEntity;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product extends DomainEntity {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @NonNull
    @Column(nullable = false, updatable = false)
    private UUID sellerId;

    @Setter
    @Positive(message = "Product price must be greater than zero")
    @Column(nullable = false)
    @Check(constraints = "price > 0")
    private double price;

    @Setter
    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 100, message = "Product title must be between {min} and {max} characters long")
    @Column(name = "title", length = 100, nullable = false)
    @Check(constraints = "LENGTH(TRIM(title)) >= 3")
    private String title;

    @Setter
    @Size(max = 500, message = "Product description cannot exceed {max} characters")
    @Column(name = "description", length = 500)
    @Check(constraints = "description IS NULL OR LENGTH(TRIM(description)) > 0")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<Variant> variants = new HashSet<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public void verifyOwnership(UUID requestingSellerId) {
        if (!this.sellerId.equals(requestingSellerId)) {
            throw new ProductNotOwnedException(this.id, requestingSellerId);
        }
    }

    private void sanitize() {
        // Strip leading and trailing whitespace from the title
        if (this.title != null) {
            this.title = this.title.trim();
        }

        // Strip leading and trailing whitespace from the description
        if (this.description != null) {
            this.description = this.description.trim();
        }

        // Store the description as null if it is blank after trimming
        if (this.description != null && this.description.isBlank()) {
            this.description = null;
        }
    }

    public void deactivate() {
        // Mark the product as inactive
        this.active = false;
    }

    public void activate() {
        // Mark the product as active
        this.active = true;
    }

    @PrePersist
    private void beforeSave() {
        this.sanitize();
    }

    @PreUpdate
    private void beforeUpdate() {
        this.sanitize();
    }
}
