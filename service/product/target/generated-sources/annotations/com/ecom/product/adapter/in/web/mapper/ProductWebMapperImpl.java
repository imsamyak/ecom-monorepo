package com.ecom.product.adapter.in.web.mapper;

import com.ecom.product.adapter.in.web.dto.request.CreateProductRequest;
import com.ecom.product.adapter.in.web.dto.request.UpdateProductRequest;
import com.ecom.product.adapter.in.web.dto.response.ProductResponse;
import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-01T05:23:09+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260826-1225, environment: Java 21.0.12.1 (Eclipse Adoptium)"
)
@Component
public class ProductWebMapperImpl implements ProductWebMapper {

    @Override
    public ProductResponse toResponse(ProductResult result) {
        if ( result == null ) {
            return null;
        }

        UUID id = null;
        UUID sellerId = null;
        String title = null;
        String description = null;
        double price = 0.0d;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = result.id();
        sellerId = result.sellerId();
        title = result.title();
        description = result.description();
        price = result.price();
        createdAt = result.createdAt();
        updatedAt = result.updatedAt();

        ProductResponse productResponse = new ProductResponse( id, sellerId, title, description, price, createdAt, updatedAt );

        return productResponse;
    }

    @Override
    public CreateProductCommand toCommand(CreateProductRequest request, UUID sellerId) {
        if ( request == null && sellerId == null ) {
            return null;
        }

        CreateProductCommand.CreateProductCommandBuilder createProductCommand = CreateProductCommand.builder();

        if ( request != null ) {
            createProductCommand.description( request.description() );
            createProductCommand.price( request.price() );
            createProductCommand.title( request.title() );
        }
        createProductCommand.sellerId( sellerId );

        return createProductCommand.build();
    }

    @Override
    public UpdateProductCommand toCommand(UpdateProductRequest request, UUID sellerId, UUID productId) {
        if ( request == null && sellerId == null && productId == null ) {
            return null;
        }

        UpdateProductCommand.UpdateProductCommandBuilder updateProductCommand = UpdateProductCommand.builder();

        if ( request != null ) {
            updateProductCommand.description( request.description() );
            updateProductCommand.price( request.price() );
            updateProductCommand.title( request.title() );
        }
        updateProductCommand.sellerId( sellerId );
        updateProductCommand.productId( productId );

        return updateProductCommand.build();
    }

    @Override
    public DeleteProductCommand toCommand(UUID sellerId, UUID productId) {
        if ( sellerId == null && productId == null ) {
            return null;
        }

        DeleteProductCommand.DeleteProductCommandBuilder deleteProductCommand = DeleteProductCommand.builder();

        deleteProductCommand.sellerId( sellerId );
        deleteProductCommand.productId( productId );

        return deleteProductCommand.build();
    }
}
