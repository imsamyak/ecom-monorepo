package com.ecom.product.service.product;

import com.ecom.product.domain.exception.ProductNotFoundException;

import com.ecom.product.port.in.usecase.product.DeleteProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.DeleteProductCommand;
import com.ecom.product.port.out.persistence.product.DeleteProductPort;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.ecom.product.domain.entity.Product;

@Service
@Validated
@RequiredArgsConstructor
public class DeleteProductService implements DeleteProductUseCase {

    private final LoadProductPort loadProductPort;
    private final DeleteProductPort deleteProductPort;

    @Override
    public void deleteProduct(DeleteProductCommand command) {
        Product product = loadProductPort.loadProduct(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        product.verifyOwnership(command.sellerId());

        deleteProductPort.deleteProduct(command.productId());
    }

}
