package com.ecom.product.domain.entity;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductActiveFlagTest {

    @Test
    void aNewProductIsActiveByDefault() {
        // Create a new product without explicitly setting the active flag
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // The product should be active
        assertTrue(product.isActive());
    }
    
    @Test
    void deactivateMakesTheProductInactive() {
        // Create an initially active product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // Deactivate the product
        product.deactivate();
        
        // The product should now be inactive
        assertFalse(product.isActive());
    }
    
    @Test
    void activateMakesAnInactiveProductActiveAgain() {
        // Create a product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // Deactivate the product to make it inactive
        product.deactivate();
        
        // Activate the product
        product.activate();
        
        // The product should be active again
        assertTrue(product.isActive());
    }
}
