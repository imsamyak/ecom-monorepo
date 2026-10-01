package com.ecom.product.service.variant.mapper;

import com.ecom.product.domain.entity.Product;
import com.ecom.product.domain.entity.Variant;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-01T05:23:09+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260826-1225, environment: Java 21.0.12.1 (Eclipse Adoptium)"
)
@Component
public class VariantMapperImpl implements VariantMapper {

    @Override
    public VariantResult toResult(Variant variant) {
        if ( variant == null ) {
            return null;
        }

        VariantResult.VariantResultBuilder variantResult = VariantResult.builder();

        variantResult.productId( variantProductId( variant ) );
        variantResult.createdAt( variant.getCreatedAt() );
        variantResult.id( variant.getId() );
        TreeMap<String, String> treeMap = variant.getProperties();
        if ( treeMap != null ) {
            variantResult.properties( new LinkedHashMap<String, String>( treeMap ) );
        }
        variantResult.updatedAt( variant.getUpdatedAt() );

        return variantResult.build();
    }

    private UUID variantProductId(Variant variant) {
        if ( variant == null ) {
            return null;
        }
        Product product = variant.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
