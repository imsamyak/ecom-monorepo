package com.ecom.product.service.product;

import com.ecom.outbox.Outbox;
import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.out.persistence.product.SaveProductPort;
import com.ecom.product.service.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CreateProductServiceOutboxTest {

    // The service under test with its ports mocked; buildOutbox does not touch them
    private final CreateProductService service =
            new CreateProductService(Mockito.mock(SaveProductPort.class), Mockito.mock(ProductMapper.class));

    @Test
    void buildOutboxDescribesTheCreatedProductAsAProductDomainEvent() {
        // A command and the result of creating that product
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        CreateProductCommand command = new CreateProductCommand(sellerId, "Phone", "desc", 10.5);
        ProductResult result = new ProductResult(productId, sellerId, "Phone", "desc", 10.5, now, now);

        // Build the outbox the way the aspect does after execute
        Outbox outbox = service.buildOutbox(command, result);

        // Type and id come from the event record, so existing consumers see the same values as before
        assertEquals("Product", outbox.getAggregateType());
        assertEquals(productId.toString(), outbox.getAggregateId());

        // The payload is the product event carrying every result field
        com.ecom.product.domain.event.Product payload =
                assertInstanceOf(com.ecom.product.domain.event.Product.class, outbox.getPayload());
        assertEquals(productId, payload.id());
        assertEquals(sellerId, payload.sellerId());
        assertEquals("Phone", payload.title());
        assertEquals("desc", payload.description());
        assertEquals(10.5, payload.price());
        assertEquals(now, payload.createdAt());
        assertEquals(now, payload.updatedAt());
    }
}
