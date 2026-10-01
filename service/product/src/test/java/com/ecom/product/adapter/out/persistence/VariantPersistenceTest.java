package com.ecom.product.adapter.out.persistence;

import com.ecom.product.domain.entity.Product;
import com.ecom.product.domain.entity.Variant;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Runs against the real JPA mapping and an in-memory H2 database
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:variantpersistence;DB_CLOSE_DELAY=-1")
class VariantPersistenceTest {

    @Autowired
    private TestEntityManager em;

    // Saves a valid product and returns it
    private Product newProduct() {
        return em.persistAndFlush(Product.builder()
                .sellerId(UUID.randomUUID()).title("Shirt").description("desc").price(10.0).build());
    }

    // Builds an unsaved variant with the given properties
    private static Variant variant(Product product, String... kv) {
        TreeMap<String, String> map = new TreeMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return Variant.builder().product(product).properties(map).build();
    }

    @Test
    void savedVariantGetsAnIdAndKeepsItsPropertiesWhenReloaded() {
        // Save a variant, then clear the session so the next read really hits the database
        Product product = newProduct();
        Variant saved = em.persistAndFlush(variant(product, "color", "red", "size", "M"));
        Long id = saved.getId();
        em.clear();

        // Reload from the database
        Variant reloaded = em.find(Variant.class, id);

        // The properties survive the round trip through the sku column
        assertNotNull(id);
        assertEquals(Map.of("color", "red", "size", "M"), reloaded.getProperties());
    }

    @Test
    void skuColumnHoldsSortedKeyColonValuePairs() {
        // Save a variant whose keys are added out of order
        Product product = newProduct();
        em.persistAndFlush(variant(product, "size", "M", "color", "red"));

        // Read the raw column value, bypassing the converter
        Object sku = em.getEntityManager().createNativeQuery("select sku from variant").getSingleResult();

        // The stored text is sorted and never empty
        assertEquals("color:red,size:M", sku);
    }

    @Test
    void twoDifferentVariantsOfTheSameProductCanBothBeSaved() {
        // Two variants with different properties on one product
        Product product = newProduct();
        em.persistAndFlush(variant(product, "color", "red"));
        em.persistAndFlush(variant(product, "color", "blue"));

        // Both rows exist
        Long count = (Long) em.getEntityManager().createQuery("select count(v) from Variant v").getSingleResult();
        assertEquals(2L, count);
    }

    @Test
    void sameVariantTwiceOnTheSameProductViolatesTheUniqueConstraint() {
        // First variant is fine
        Product product = newProduct();
        em.persistAndFlush(variant(product, "color", "red"));

        // An identical second one on the same product is a duplicate sku
        assertThrows(PersistenceException.class, () -> em.persistAndFlush(variant(product, "color", "red")));
    }

    @Test
    void sameVariantOnDifferentProductsIsAllowed() {
        // Same properties, two different products
        em.persistAndFlush(variant(newProduct(), "color", "red"));
        em.persistAndFlush(variant(newProduct(), "color", "red"));

        // The unique constraint is per product, so both rows exist
        Long count = (Long) em.getEntityManager().createQuery("select count(v) from Variant v").getSingleResult();
        assertEquals(2L, count);
    }

    @Test
    void emptyPropertiesAreRejectedByTheDatabase() {
        // A variant with no properties would store an empty sku
        Product product = newProduct();

        // The database check refuses it
        assertThrows(PersistenceException.class, () -> em.persistAndFlush(variant(product)));
    }

    @Test
    void propertiesThatAreAllBlankAreRejectedByTheDatabase() {
        // Blank keys and values are dropped by the converter, leaving an empty sku
        Product product = newProduct();

        // The database check refuses it
        assertThrows(PersistenceException.class, () -> em.persistAndFlush(variant(product, " ", "x", "size", " ")));
    }

    @Test
    void databaseRejectsAnEmptySkuEvenWhenWrittenByRawSql() {
        // Bypass the entity and converter completely
        Product product = newProduct();

        // An empty sku in raw SQL is refused by the check constraint itself
        assertThrows(PersistenceException.class, () -> em.getEntityManager().createNativeQuery(
                "insert into variant (id, product_id, sku, version, created_at, updated_at) "
                        + "values (next value for variant_seq, ?, '', 0, current_timestamp, current_timestamp)")
                .setParameter(1, product.getId()).executeUpdate());
    }

    @Test
    void databaseRejectsAWhitespaceOnlySkuWrittenByRawSql() {
        // Bypass the entity and converter completely
        Product product = newProduct();

        // Spaces count as empty, matching how product titles are checked
        assertThrows(PersistenceException.class, () -> em.getEntityManager().createNativeQuery(
                "insert into variant (id, product_id, sku, version, created_at, updated_at) "
                        + "values (next value for variant_seq, ?, '   ', 0, current_timestamp, current_timestamp)")
                .setParameter(1, product.getId()).executeUpdate());
    }
}
