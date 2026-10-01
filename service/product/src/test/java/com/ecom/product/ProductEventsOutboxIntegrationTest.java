package com.ecom.product;

import com.ecom.outbox.entity.OutboxEntity;
import com.ecom.outbox.repository.OutboxRepository;
import com.ecom.product.domain.exception.ProductNotFoundException;
import com.ecom.product.domain.exception.ProductNotOwnedException;
import com.ecom.product.port.in.usecase.product.CreateProductUseCase;
import com.ecom.product.port.in.usecase.product.DeleteProductUseCase;
import com.ecom.product.port.in.usecase.product.UpdateProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.in.usecase.variant.AddVariantUseCase;
import com.ecom.product.port.in.usecase.variant.RemoveVariantUseCase;
import com.ecom.product.port.in.usecase.variant.dto.command.AddVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.command.RemoveVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// The relay is switched off so the rows stay in the outbox table for inspection
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:productevents", "outbox.relay.enabled=false"})
class ProductEventsOutboxIntegrationTest {

    @Autowired CreateProductUseCase createProduct;
    @Autowired UpdateProductUseCase updateProduct;
    @Autowired DeleteProductUseCase deleteProduct;
    @Autowired AddVariantUseCase addVariant;
    @Autowired RemoveVariantUseCase removeVariant;
    @Autowired OutboxRepository outboxRepository;
    @Autowired ObjectMapper mapper;

    private final UUID sellerId = UUID.randomUUID();

    @BeforeEach
    void clean() {
        // Every test starts with an empty outbox
        outboxRepository.deleteAll();
    }

    // Creates a product and clears the CREATE row so a test only sees the rows of its own action
    private ProductResult newProduct() {
        ProductResult result = createProduct.execute(new CreateProductCommand(sellerId, "Phone", "desc", 10.5));
        outboxRepository.deleteAll();
        return result;
    }

    // Returns the only outbox row, failing if there is not exactly one
    private OutboxEntity onlyRow() {
        List<OutboxEntity> rows = new ArrayList<>(outboxRepository.findAll());
        rows.sort(Comparator.comparing(OutboxEntity::getCreatedAt));
        assertEquals(1, rows.size(), "expected exactly one outbox row but found " + rows.size());
        return rows.get(0);
    }

    private JsonNode payload(OutboxEntity row) throws Exception {
        return mapper.readTree(row.getPayload());
    }

    private static Set<String> keys(JsonNode node) {
        return StreamSupport.stream(((Iterable<String>) node::fieldNames).spliterator(), false).collect(Collectors.toSet());
    }

    @Test
    void creatingAProductWritesAProductCreatedRow() throws Exception {
        // Create a product through the use case
        ProductResult result = createProduct.execute(new CreateProductCommand(sellerId, "Phone", "desc", 10.5));

        // One row: type, id, and an envelope with the full snapshot as data
        OutboxEntity row = onlyRow();
        assertEquals("Product", row.getAggregateType());
        assertEquals(result.id().toString(), row.getAggregateId());
        JsonNode json = payload(row);
        assertEquals(Set.of("aggregate", "action", "data"), keys(json));
        assertEquals("Product", json.get("aggregate").asText());
        assertEquals("CREATE", json.get("action").asText());
        assertEquals(Set.of("productId", "sellerId", "title", "description", "price", "createdAt", "updatedAt", "status"),
                keys(json.get("data")));
        assertEquals(result.id().toString(), json.get("data").get("productId").asText());
        assertEquals("Phone", json.get("data").get("title").asText());
        assertEquals("INACTIVE", json.get("data").get("status").asText());
    }

    @Test
    void updatingAProductWritesAProductUpdatedRowWithTheNewValues() throws Exception {
        // An existing product, then an update
        ProductResult product = newProduct();
        updateProduct.execute(new UpdateProductCommand(product.id(), sellerId, 
                Optional.of("Phone 2"), Optional.of("new"), Optional.of(20.0), null));

        // One UPDATE row with the values after the change
        OutboxEntity row = onlyRow();
        assertEquals("Product", row.getAggregateType());
        assertEquals(product.id().toString(), row.getAggregateId());
        JsonNode json = payload(row);
        assertEquals("UPDATE", json.get("action").asText());
        assertEquals(Set.of("productId", "sellerId", "title", "description", "price", "createdAt", "updatedAt", "status"),
                keys(json.get("data")));
        assertEquals("Phone 2", json.get("data").get("title").asText());
        assertEquals(20.0, json.get("data").get("price").asDouble());
        // It should carry the INACTIVE status as the product hasn't been activated
        assertEquals("INACTIVE", json.get("data").get("status").asText());
    }

    @Test
    void deletingAProductWritesAProductDeletedRowWithOnlyTheIdentifiers() throws Exception {
        // An existing product, then a delete
        ProductResult product = newProduct();
        deleteProduct.execute(new DeleteProductCommand(product.id(), sellerId));

        // One DELETE row with just the ids
        OutboxEntity row = onlyRow();
        assertEquals("Product", row.getAggregateType());
        assertEquals(product.id().toString(), row.getAggregateId());
        JsonNode json = payload(row);
        assertEquals("DELETE", json.get("action").asText());
        assertEquals(Set.of("productId", "sellerId"), keys(json.get("data")));
    }

    @Test
    void addingAVariantWritesAVariantAddedRowKeyedByTheProductId() throws Exception {
        // An existing product, then a variant
        ProductResult product = newProduct();
        VariantResult variant = addVariant.execute(new AddVariantCommand(product.id(), sellerId, Map.of("color", "red")));

        // One ADD row whose aggregate id is the product id, not the variant id
        OutboxEntity row = onlyRow();
        assertEquals("Variant", row.getAggregateType());
        assertEquals(product.id().toString(), row.getAggregateId());
        JsonNode json = payload(row);
        assertEquals("Variant", json.get("aggregate").asText());
        assertEquals("ADD", json.get("action").asText());
        assertEquals(variant.id(), json.get("data").get("variantId").asLong());
        assertEquals("red", json.get("data").get("properties").get("color").asText());
        assertEquals(Set.of("variantId", "productId", "properties", "createdAt", "updatedAt"), keys(json.get("data")));
    }

    @Test
    void removingAVariantWritesAVariantRemovedRowAndReturnsTheRemovedVariant() throws Exception {
        // A product with one variant, then remove the variant
        ProductResult product = newProduct();
        VariantResult variant = addVariant.execute(new AddVariantCommand(product.id(), sellerId, Map.of("color", "red")));
        outboxRepository.deleteAll();
        VariantResult removed = removeVariant.execute(new RemoveVariantCommand(product.id(), variant.id(), sellerId));

        // The removed variant's data is returned
        assertEquals(variant.id(), removed.id());
        assertEquals(Map.of("color", "red"), removed.properties());

        // One REMOVE row, keyed by the product id, carrying what was removed
        OutboxEntity row = onlyRow();
        assertEquals("Variant", row.getAggregateType());
        assertEquals(product.id().toString(), row.getAggregateId());
        JsonNode json = payload(row);
        assertEquals("REMOVE", json.get("action").asText());
        assertEquals(Set.of("variantId", "productId", "properties"), keys(json.get("data")));
        assertEquals("red", json.get("data").get("properties").get("color").asText());
    }

    @Test
    void aFailedUpdateOfAnUnknownProductWritesNoRow() {
        // The product does not exist
        assertThrows(ProductNotFoundException.class,
                () -> updateProduct.execute(new UpdateProductCommand(UUID.randomUUID(), sellerId, 
                        Optional.of("Phone"), Optional.of("d"), Optional.of(1.0), null)));

        // The business rule failed, so no event was recorded
        assertEquals(0, outboxRepository.count());
    }

    @Test
    void aFailedUpdateByAnotherSellerWritesNoRow() {
        // A product owned by someone else
        ProductResult product = newProduct();

        // The update is refused and no event is recorded
        assertThrows(ProductNotOwnedException.class,
                () -> updateProduct.execute(new UpdateProductCommand(product.id(), UUID.randomUUID(), 
                        Optional.of("Hacked"), Optional.of("d"), Optional.of(1.0), null)));
        assertEquals(0, outboxRepository.count());
    }

    @Test
    void deleteStillValidatesItsCommand() {
        // A command with a missing product id
        assertThrows(ConstraintViolationException.class,
                () -> deleteProduct.execute(new DeleteProductCommand(null, sellerId)));

        // Nothing is recorded
        assertEquals(0, outboxRepository.count());
    }

    @Test
    void removeVariantStillValidatesItsCommand() {
        // A command with a missing product id
        assertThrows(ConstraintViolationException.class,
                () -> removeVariant.execute(new RemoveVariantCommand(null, 1L, sellerId)));

        // Nothing is recorded
        assertEquals(0, outboxRepository.count());
    }
}
