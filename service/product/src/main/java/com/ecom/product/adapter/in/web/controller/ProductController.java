package com.ecom.product.adapter.in.web.controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;

import com.ecom.product.adapter.in.web.dto.request.CreateProductRequest;
import com.ecom.product.adapter.in.web.dto.request.UpdateProductRequest;
import com.ecom.product.adapter.in.web.dto.response.ProductResponse;
import com.ecom.product.adapter.in.web.mapper.ProductWebMapper;

import com.ecom.shared.security.JwtPrincipal;
import com.ecom.product.port.in.usecase.product.CreateProductUseCase;
import com.ecom.product.port.in.usecase.product.UpdateProductUseCase;
import com.ecom.product.port.in.usecase.product.DeleteProductUseCase;

import com.ecom.product.port.in.usecase.product.dto.command.CreateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.UpdateProductCommand;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;

import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    private final ProductWebMapper productWebMapper;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody CreateProductRequest request) {

        CreateProductCommand command = productWebMapper.toCommand(request, principal.userId());

        ProductResult result = createProductUseCase.execute(command);
        return new ResponseEntity<>(productWebMapper.toResponse(result), HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody UpdateProductRequest request) {

        UpdateProductCommand command = productWebMapper.toCommand(request, principal.userId(), productId);
        ProductResult result = updateProductUseCase.updateProduct(command);
        return ResponseEntity.ok(productWebMapper.toResponse(result));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        DeleteProductCommand command = productWebMapper.toCommand(principal.userId(), productId);

        deleteProductUseCase.deleteProduct(command);
        return ResponseEntity.noContent().build();
    }
}

