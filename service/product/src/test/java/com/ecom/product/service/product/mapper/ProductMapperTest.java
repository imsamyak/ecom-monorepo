package com.ecom.product.service.product.mapper;

import com.ecom.contract.event.ProductEvent;
import com.ecom.product.domain.enums.ProductStatus;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void shouldMapProductResultToCreateEvent() {
        // given
        UUID id = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ProductResult result = ProductResult.builder()
                .id(id)
                .sellerId(sellerId)
                .title("Title")
                .description("Desc")
                .price(10.0)
                .createdAt(now)
                .updatedAt(now)
                .status(ProductStatus.ACTIVE)
                .build();

        // when
        ProductEvent.CREATE event = mapper.toCreateEvent(result);

        // then
        assertThat(event.productId()).isEqualTo(id);
        assertThat(event.sellerId()).isEqualTo(sellerId);
        assertThat(event.title()).isEqualTo("Title");
        assertThat(event.description()).isEqualTo("Desc");
        assertThat(event.price()).isEqualTo(10.0);
        assertThat(event.createdAt()).isEqualTo(now);
        assertThat(event.updatedAt()).isEqualTo(now);
        assertThat(event.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldMapProductResultToUpdateEvent() {
        // given
        UUID id = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ProductResult result = ProductResult.builder()
                .id(id)
                .sellerId(sellerId)
                .title("Title")
                .description("Desc")
                .price(10.0)
                .createdAt(now)
                .updatedAt(now)
                .status(ProductStatus.INACTIVE)
                .build();

        // when
        ProductEvent.UPDATE event = mapper.toUpdateEvent(result);

        // then
        assertThat(event.productId()).isEqualTo(id);
        assertThat(event.sellerId()).isEqualTo(sellerId);
        assertThat(event.title()).isEqualTo("Title");
        assertThat(event.description()).isEqualTo("Desc");
        assertThat(event.price()).isEqualTo(10.0);
        assertThat(event.createdAt()).isEqualTo(now);
        assertThat(event.updatedAt()).isEqualTo(now);
        assertThat(event.status()).isEqualTo("INACTIVE");
    }

    @Test
    void shouldMapDeleteProductCommandToDeleteEvent() {
        // given
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        DeleteProductCommand command = DeleteProductCommand.builder()
                .productId(productId)
                .sellerId(sellerId)
                .build();

        // when
        ProductEvent.DELETE event = mapper.toDeleteEvent(command);

        // then
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.sellerId()).isEqualTo(sellerId);
    }
}
