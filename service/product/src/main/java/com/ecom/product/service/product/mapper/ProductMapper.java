package com.ecom.product.service.product.mapper;

import com.ecom.product.domain.entity.Product;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "createdAt", expression = "java(product.getCreatedAt())")
    @Mapping(target = "updatedAt", expression = "java(product.getUpdatedAt())")
    ProductResult toResult(Product product);
}

