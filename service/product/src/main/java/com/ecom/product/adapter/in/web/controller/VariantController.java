package com.ecom.product.adapter.in.web.controller;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;

import com.ecom.product.adapter.in.web.dto.request.AddVariantRequest;
import com.ecom.product.adapter.in.web.dto.response.VariantResponse;
import com.ecom.product.adapter.in.web.mapper.VariantWebMapper;
import com.ecom.shared.security.JwtPrincipal;

import com.ecom.product.port.in.usecase.variant.AddVariantUseCase;
import com.ecom.product.port.in.usecase.variant.RemoveVariantUseCase;
import com.ecom.product.port.in.usecase.variant.dto.command.AddVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.command.RemoveVariantCommand;
import com.ecom.product.port.in.usecase.variant.dto.result.VariantResult;

import java.util.UUID;

@RestController 
@RequestMapping("/products/{productId}/variants")
@RequiredArgsConstructor
public class VariantController {

    private final AddVariantUseCase addVariantUseCase;
    private final RemoveVariantUseCase removeVariantUseCase;
    private final VariantWebMapper variantWebMapper;

    @PostMapping
    public ResponseEntity<VariantResponse> addVariant(
            @PathVariable UUID productId,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody AddVariantRequest request) {
        
        AddVariantCommand command = AddVariantCommand.builder()
                .productId(productId)
                .sellerId(principal.userId())
                .properties(request.properties())
                .build();
                
        VariantResult result = addVariantUseCase.addVariant(command);
        return new ResponseEntity<>(variantWebMapper.toResponse(result), HttpStatus.CREATED);
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> removeVariant(
            @PathVariable UUID productId,
            @PathVariable Long variantId,
            @AuthenticationPrincipal JwtPrincipal principal) {
            
        RemoveVariantCommand command = RemoveVariantCommand.builder()
                .productId(productId)
                .variantId(variantId)
                .sellerId(principal.userId())
                .build();
                
        removeVariantUseCase.removeVariant(command);
        return ResponseEntity.noContent().build();
    }
}


