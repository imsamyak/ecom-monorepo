package com.ecom.product.adapter.in.web.mapper;

import com.ecom.product.adapter.in.web.dto.request.CreateProductRequest;
import com.ecom.product.adapter.in.web.dto.request.UpdateProductRequest;
import com.ecom.product.adapter.in.web.dto.request.SetProductActiveRequest;
import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.SetProductActiveCommand;
import com.ecom.product.adapter.in.web.dto.response.ProductResponse;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;

import jakarta.validation.Valid;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {
    ProductResponse toResponse(ProductResult result);

    @Mapping(target = "sellerId", source = "sellerId")
    CreateProductCommand toCommand(@Valid CreateProductRequest request, UUID sellerId);

    @Mapping(target = "sellerId", source = "sellerId")
    @Mapping(target = "productId", source = "productId")
    UpdateProductCommand toCommand(UpdateProductRequest request, UUID sellerId, UUID productId);

    @Mapping(target = "sellerId", source = "sellerId")
    @Mapping(target = "productId", source = "productId")
    DeleteProductCommand toCommand(UUID sellerId, UUID productId);

    @Mapping(target = "sellerId", source = "sellerId")
    @Mapping(target = "productId", source = "productId")
    SetProductActiveCommand toCommand(SetProductActiveRequest request, UUID sellerId, UUID productId);
}


