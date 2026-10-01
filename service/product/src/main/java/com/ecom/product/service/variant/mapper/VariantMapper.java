package com.ecom.product.service.variant.mapper;

import com.ecom.product.domain.entity.Variant;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import com.ecom.contract.event.VariantEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VariantMapper {
    
    // MapStruct needs to know how to get productId since it's nested in the Product entity
    @Mapping(target = "productId", source = "product.id")
    VariantResult toResult(Variant variant);

    @Mapping(target = "variantId", source = "id")
    VariantEvent.ADD toAddEvent(VariantResult result);

    @Mapping(target = "variantId", source = "id")
    VariantEvent.REMOVE toRemoveEvent(VariantResult result);
}

