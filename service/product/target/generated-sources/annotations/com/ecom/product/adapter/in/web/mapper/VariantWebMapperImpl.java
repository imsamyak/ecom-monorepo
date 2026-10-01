package com.ecom.product.adapter.in.web.mapper;

import com.ecom.product.adapter.in.web.dto.response.VariantResponse;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-01T05:23:09+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260826-1225, environment: Java 21.0.12.1 (Eclipse Adoptium)"
)
@Component
public class VariantWebMapperImpl implements VariantWebMapper {

    @Override
    public VariantResponse toResponse(VariantResult result) {
        if ( result == null ) {
            return null;
        }

        Long id = null;
        UUID productId = null;
        Map<String, String> properties = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = result.id();
        productId = result.productId();
        Map<String, String> map = result.properties();
        if ( map != null ) {
            properties = new LinkedHashMap<String, String>( map );
        }
        createdAt = result.createdAt();
        updatedAt = result.updatedAt();

        VariantResponse variantResponse = new VariantResponse( id, productId, properties, createdAt, updatedAt );

        return variantResponse;
    }
}
