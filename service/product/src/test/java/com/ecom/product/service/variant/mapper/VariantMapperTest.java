package com.ecom.product.service.variant.mapper;

import com.ecom.contract.event.VariantEvent;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VariantMapperTest {

    private final VariantMapper mapper = Mappers.getMapper(VariantMapper.class);

    @Test
    void shouldMapVariantResultToAddEvent() {
        // given
        Long id = 1L;
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> properties = Map.of("color", "red");
        VariantResult result = VariantResult.builder()
                .id(id)
                .productId(productId)
                .properties(properties)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // when
        VariantEvent.ADD event = mapper.toAddEvent(result);

        // then
        assertThat(event.variantId()).isEqualTo(id);
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.properties()).isEqualTo(properties);
        assertThat(event.createdAt()).isEqualTo(now);
        assertThat(event.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapVariantResultToRemoveEvent() {
        // given
        Long id = 1L;
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> properties = Map.of("color", "red");
        VariantResult result = VariantResult.builder()
                .id(id)
                .productId(productId)
                .properties(properties)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // when
        VariantEvent.REMOVE event = mapper.toRemoveEvent(result);

        // then
        assertThat(event.variantId()).isEqualTo(id);
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.properties()).isEqualTo(properties);
    }
}
