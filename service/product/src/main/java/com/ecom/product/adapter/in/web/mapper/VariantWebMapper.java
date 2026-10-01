package com.ecom.product.adapter.in.web.mapper;

import com.ecom.product.adapter.in.web.dto.response.VariantResponse;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VariantWebMapper {
    VariantResponse toResponse(VariantResult result);
}

