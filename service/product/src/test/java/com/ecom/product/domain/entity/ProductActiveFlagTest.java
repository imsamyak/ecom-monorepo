package com.ecom.product.domain.entity;

import com.ecom.product.domain.enums.ProductStatus;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductActiveFlagTest {

    @Test
    void aNewProductIsInactiveByDefault() {
        // Create a new product without explicitly setting the status
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // The product should be inactive by default
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }
    
    @Test
    void activateMakesAnInactiveProductActive() {
        // Create a product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // Activate the product
        product.activate();
        
        // The product should now be active
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }
    
    @Test
    void deactivateMakesAnActiveProductInactiveAgain() {
        // Create a product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // Activate the product to make it active
        product.activate();
        
        // Deactivate the product
        product.deactivate();
        
        // The product should be inactive again
        assertEquals(ProductStatus.INACTIVE, product.getStatus());
    }
}
