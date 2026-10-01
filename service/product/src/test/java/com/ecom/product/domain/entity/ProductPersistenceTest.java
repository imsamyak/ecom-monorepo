package com.ecom.product.domain.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// Runs against the real JPA mapping and an in-memory H2 database
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:productpersistence;DB_CLOSE_DELAY=-1")
class ProductPersistenceTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void titleWithSurroundingWhitespaceIsTrimmedOnInsert() {
        // Create a product with whitespace around the title
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("  Shoe  ")
                .price(10.0)
                .build();

        // Save the product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // The title is trimmed to remove leading and trailing whitespace
        assertEquals("Shoe", reloaded.getTitle());
    }

    @Test
    void descriptionWithSurroundingWhitespaceIsTrimmedOnInsert() {
        // Create a product with whitespace around the description
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .description("  nice  ")
                .build();

        // Save the product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // The description is trimmed to remove leading and trailing whitespace
        assertEquals("nice", reloaded.getDescription());
    }

    @Test
    void blankDescriptionIsStoredAsNull() {
        // Create a product with a description containing only whitespace
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .description("   ")
                .build();

        // Save the product to the database and clear the session
        Product saved = em.persistAndFlush(product);
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // The blank description is converted to null during the sanitize step
        assertNull(reloaded.getDescription());
    }

    @Test
    void updatingAProductReAppliesTrimmingOfTitleAndDescription() {
        // Create and save an initial product with clean values
        Product product = Product.builder()
                .sellerId(UUID.randomUUID())
                .title("Shoe")
                .price(10.0)
                .description("nice")
                .build();
        Product saved = em.persistAndFlush(product);
        
        // Update the title and description with new values wrapped in whitespace
        saved.setTitle("  Hat  ");
        saved.setDescription("  blue  ");
        
        // Flush the updates to the database and clear the session
        em.flush();
        em.clear();

        // Reload the product from the database
        Product reloaded = em.find(Product.class, saved.getId());

        // The updated title and description are trimmed before saving
        assertEquals("Hat", reloaded.getTitle());
        assertEquals("blue", reloaded.getDescription());
    }
}
