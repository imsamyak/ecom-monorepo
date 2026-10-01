package com.ecom.product.domain.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Runs against the real JPA mapping and an in-memory H2 database
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:productactiveflag;DB_CLOSE_DELAY=-1")
class ProductActiveFlagPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void aProductSavedByDefaultIsActiveInTheDatabase() {
        // Create a new product without explicitly setting the active flag
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();

        // Save the product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // The reloaded product should be active by default
        assertTrue(reloaded.isActive());
    }

    @Test
    void theActiveFlagSurvivesSaveAndReload() {
        // Create a product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // Deactivate the product so it is inactive
        product.deactivate();

        // Save the product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // The reloaded product should still be inactive
        assertFalse(reloaded.isActive());
    }

    @Test
    void reactivatingASavedInactiveProductIsPersisted() {
        // Create a product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();

        // Deactivate the product so it is initially inactive
        product.deactivate();

        // Save the inactive product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // Activate the reloaded product
        reloaded.activate();

        // Flush the updates to the database and clear the session
        em.flush();
        em.clear();

        // Reload the product again from the database
        Product reloadedAgain = em.find(Product.class, saved.getId());

        // The reloaded product should now be active
        assertTrue(reloadedAgain.isActive());
    }
}
