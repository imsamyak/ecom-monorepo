package com.ecom.product.domain.entity;

import com.ecom.product.domain.enums.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Runs against the real JPA mapping and an in-memory H2 database
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:productactiveflag;DB_CLOSE_DELAY=-1")
class ProductActiveFlagPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void aProductSavedByDefaultIsInactiveInTheDatabase() {
        // Create a new product without explicitly setting the status
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

        // The reloaded product should be inactive by default
        assertEquals(ProductStatus.INACTIVE, reloaded.getStatus());

        // Read the native status column directly to verify it holds the text INACTIVE
        String dbStatus = (String) em.getEntityManager().createNativeQuery(
                "SELECT status FROM product WHERE id = :id")
                .setParameter("id", saved.getId())
                .getSingleResult();
        assertEquals("INACTIVE", dbStatus);
    }

    @Test
    void theStatusSurvivesSaveAndReload() {
        // Create a product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();
                
        // Activate the product so it is active
        product.activate();

        // Save the product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // The reloaded product should still be active
        assertEquals(ProductStatus.ACTIVE, reloaded.getStatus());

        // Read the native status column directly to verify it holds the text ACTIVE
        String dbStatus = (String) em.getEntityManager().createNativeQuery(
                "SELECT status FROM product WHERE id = :id")
                .setParameter("id", saved.getId())
                .getSingleResult();
        assertEquals("ACTIVE", dbStatus);
    }

    @Test
    void deactivatingASavedActiveProductIsPersisted() {
        // Create a product
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .build();

        // Activate the product so it is initially active
        product.activate();

        // Save the active product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // Deactivate the reloaded product
        reloaded.deactivate();

        // Flush the updates to the database and clear the session
        em.flush();
        em.clear();

        // Reload the product again from the database
        Product reloadedAgain = em.find(Product.class, saved.getId());

        // The reloaded product should now be inactive
        assertEquals(ProductStatus.INACTIVE, reloadedAgain.getStatus());
    }
}
