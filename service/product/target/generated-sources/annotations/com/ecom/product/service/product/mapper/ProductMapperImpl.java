package com.ecom.product.service.product.mapper;

import com.ecom.product.domain.entity.Product;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-01T05:23:09+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260826-1225, environment: Java 21.0.12.1 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResult toResult(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResult.ProductResultBuilder productResult = ProductResult.builder();

        productResult.description( product.getDescription() );
        productResult.id( product.getId() );
        productResult.price( product.getPrice() );
        productResult.sellerId( product.getSellerId() );
        productResult.title( product.getTitle() );

        productResult.createdAt( product.getCreatedAt() );
        productResult.updatedAt( product.getUpdatedAt() );

        return productResult.build();
    }
}
