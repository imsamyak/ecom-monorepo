package com.ecom.product.service;

import com.ecom.contract.DomainEvent;
import com.ecom.contract.event.Product;
import com.ecom.contract.event.Variant;
import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.in.usecase.variant.dto.command.AddVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.command.RemoveVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import com.ecom.product.port.out.persistence.product.DeleteProductPort;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.product.port.out.persistence.product.SaveProductPort;
import com.ecom.product.port.out.persistence.variant.DeleteVariantPort;
import com.ecom.product.port.out.persistence.variant.LoadVariantPort;
import com.ecom.product.port.out.persistence.variant.SaveVariantPort;
import com.ecom.product.service.product.CreateProductService;
import com.ecom.product.service.product.DeleteProductService;
import com.ecom.product.service.product.UpdateProductService;
import com.ecom.product.service.product.mapper.ProductMapper;
import com.ecom.product.service.variant.AddVariantService;
import com.ecom.product.service.variant.RemoveVariantService;
import com.ecom.product.service.variant.mapper.VariantMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UseCaseEventsTest {

    // A real validator, as the two services validate their own command
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
    private final ProductResult productResult = new ProductResult(productId, sellerId, "Phone", "desc", 10.5, now, now);
    private final VariantResult variantResult = new VariantResult(7L, productId, Map.of("color", "red"), now, now);

    @Test
    void createProductDescribesTheProductAsACreatedEvent() {
        // The service under test, with its ports mocked
        CreateProductService service = new CreateProductService(
                Mockito.mock(SaveProductPort.class), Mockito.mock(ProductMapper.class));

        // Ask it what happened
        DomainEvent event = service.buildEvent(new CreateProductCommand(sellerId, "Phone", "desc", 10.5), productResult);

        // A CREATE event carrying the full product snapshot
        assertEquals(new Product.CREATE(productId, sellerId, "Phone", "desc", 10.5, now, now), event);
    }

    @Test
    void updateProductDescribesTheProductAsAnUpdatedEvent() {
        // The service under test, with its ports mocked
        UpdateProductService service = new UpdateProductService(Mockito.mock(LoadProductPort.class),
                Mockito.mock(SaveProductPort.class), Mockito.mock(ProductMapper.class));

        // Ask it what happened
        DomainEvent event = service.buildEvent(
                new UpdateProductCommand(productId, sellerId, "Phone", "desc", 10.5), productResult);

        // An UPDATE event carrying the full product snapshot after the change
        assertEquals(new Product.UPDATE(productId, sellerId, "Phone", "desc", 10.5, now, now), event);
    }

    @Test
    void deleteProductDescribesTheProductAsADeletedEventFromTheCommandAlone() {
        // The service under test, with its ports mocked
        DeleteProductService service = new DeleteProductService(
                Mockito.mock(LoadProductPort.class), Mockito.mock(DeleteProductPort.class), validator);

        // Delete has no result, so the event comes from the command
        DomainEvent event = service.buildEvent(new DeleteProductCommand(productId, sellerId), null);

        // A DELETE event with only the identifiers
        assertEquals(new Product.DELETE(productId, sellerId), event);
    }

    @Test
    void addVariantDescribesTheVariantAsAnAddedEventKeyedByProduct() {
        // The service under test, with its ports mocked
        AddVariantService service = new AddVariantService(Mockito.mock(LoadProductPort.class),
                Mockito.mock(SaveVariantPort.class), Mockito.mock(VariantMapper.class));

        // Ask it what happened
        DomainEvent event = service.buildEvent(
                new AddVariantCommand(productId, sellerId, Map.of("color", "red")), variantResult);

        // An ADD event whose aggregate id is the product id
        assertEquals(new Variant.ADD(7L, productId, Map.of("color", "red"), now, now), event);
        assertEquals(productId.toString(), event.aggregateId());
    }

    @Test
    void removeVariantDescribesTheVariantAsARemovedEventFromTheRemovedVariantData() {
        // The service under test, with its ports mocked
        RemoveVariantService service = new RemoveVariantService(Mockito.mock(LoadVariantPort.class),
                Mockito.mock(DeleteVariantPort.class), Mockito.mock(VariantMapper.class), validator);

        // The result of a remove is the data of the variant that was removed
        DomainEvent event = service.buildEvent(new RemoveVariantCommand(productId, 7L, sellerId), variantResult);

        // A REMOVE event carrying what was removed
        assertEquals(new Variant.REMOVE(7L, productId, Map.of("color", "red")), event);
    }

    @Test
    void removeVariantReturnsTheDataOfTheVariantItDeleted() {
        // A stored variant that belongs to a product owned by the seller
        com.ecom.product.domain.entity.Product product = com.ecom.product.domain.entity.Product.builder()
                .id(productId).sellerId(sellerId).title("Phone").price(1).build();
        com.ecom.product.domain.entity.Variant variant = com.ecom.product.domain.entity.Variant.builder()
                .id(7L).product(product).properties(new TreeMap<>(Map.of("color", "red"))).build();
        LoadVariantPort load = Mockito.mock(LoadVariantPort.class);
        DeleteVariantPort delete = Mockito.mock(DeleteVariantPort.class);
        Mockito.when(load.loadVariant(7L)).thenReturn(Optional.of(variant));
        RemoveVariantService service = new RemoveVariantService(load, delete, Mappers.getMapper(VariantMapper.class), validator);

        // Remove it
        VariantResult result = service.execute(new RemoveVariantCommand(productId, 7L, sellerId));

        // The variant is deleted and its data is returned so the event can carry it
        Mockito.verify(delete).deleteVariant(7L);
        assertEquals(7L, result.id());
        assertEquals(productId, result.productId());
        assertEquals(Map.of("color", "red"), result.properties());
    }

    @Test
    void deleteProductReturnsNothing() {
        // A stored product owned by the seller
        com.ecom.product.domain.entity.Product product = com.ecom.product.domain.entity.Product.builder()
                .id(productId).sellerId(sellerId).title("Phone").price(1).build();
        LoadProductPort load = Mockito.mock(LoadProductPort.class);
        DeleteProductPort delete = Mockito.mock(DeleteProductPort.class);
        Mockito.when(load.loadProduct(productId)).thenReturn(Optional.of(product));
        DeleteProductService service = new DeleteProductService(load, delete, validator);

        // Delete it
        Void result = service.execute(new DeleteProductCommand(productId, sellerId));

        // The product is deleted and there is no result value
        Mockito.verify(delete).deleteProduct(productId);
        assertNull(result);
    }
}
