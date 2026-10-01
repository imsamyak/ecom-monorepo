package com.ecom.product.service.product;

import com.ecom.product.domain.entity.Product;
import com.ecom.product.domain.exception.ProductNotFoundException;
import com.ecom.product.port.in.usecase.product.SetProductActiveUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.SetProductActiveCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.product.port.out.persistence.product.SaveProductPort;
import com.ecom.product.service.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class SetProductActiveService implements SetProductActiveUseCase {

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final ProductMapper productMapper;

    @Override
    public ProductResult execute(SetProductActiveCommand command) {
        // Load the product or throw an exception if it does not exist
        Product product = loadProductPort.loadProduct(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // Reject the request if the seller does not own this product
        product.verifyOwnership(command.sellerId());

        // Update the active state based on the command
        if (command.active()) {
            product.activate();
        } else {
            product.deactivate();
        }

        // Save the updated product
        Product savedProduct = saveProductPort.saveProduct(product);

        // Map the saved product to a result
        return productMapper.toResult(savedProduct);
    }
}
