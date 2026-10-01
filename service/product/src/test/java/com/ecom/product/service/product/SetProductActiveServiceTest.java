package com.ecom.product.service.product;

import com.ecom.product.domain.entity.Product;
import com.ecom.product.domain.enums.ProductStatus;
import com.ecom.product.domain.exception.ProductNotFoundException;
import com.ecom.product.domain.exception.ProductNotOwnedException;
import com.ecom.product.port.in.usecase.product.dto.command.SetProductActiveCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.product.port.out.persistence.product.SaveProductPort;
import com.ecom.product.service.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetProductActiveServiceTest {

    @Mock
    private LoadProductPort loadProductPort;

    @Mock
    private SaveProductPort saveProductPort;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private SetProductActiveService service;

    @Test
    void throwsExceptionAndSavesNothingWhenProductIsNotFound() {
        // Create random IDs for the product and seller
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        SetProductActiveCommand command = new SetProductActiveCommand(productId, sellerId, false);

        // Simulate the product not existing in the database
        when(loadProductPort.loadProduct(productId)).thenReturn(Optional.empty());

        // Verify that calling the service throws a ProductNotFoundException
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ProductNotFoundException.class);

        // Ensure that we do not attempt to save anything when the product is missing
        verify(saveProductPort, never()).saveProduct(any());
    }

    @Test
    void throwsExceptionAndSavesNothingWhenSellerDoesNotOwnProduct() {
        // Create a product owned by one seller
        UUID productId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Product product = Product.builder().id(productId).sellerId(ownerId).build();

        // Create a command from a different seller
        UUID differentSellerId = UUID.randomUUID();
        SetProductActiveCommand command = new SetProductActiveCommand(productId, differentSellerId, false);

        // Simulate finding the product
        when(loadProductPort.loadProduct(productId)).thenReturn(Optional.of(product));

        // Verify that calling the service throws a ProductNotOwnedException
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(ProductNotOwnedException.class);

        // Ensure that we do not attempt to save the product when ownership fails
        verify(saveProductPort, never()).saveProduct(any());
    }

    @Test
    void deactivatingSavesAnInactiveProduct() {
        // Create an active product
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Product product = Product.builder().id(productId).sellerId(sellerId).status(ProductStatus.ACTIVE).build();
        SetProductActiveCommand command = new SetProductActiveCommand(productId, sellerId, false);

        // Prepare the mocked returned result
        ProductResult mockResult = ProductResult.builder().id(productId).status(ProductStatus.INACTIVE).build();

        // Simulate finding the product, saving it, and mapping the result
        when(loadProductPort.loadProduct(productId)).thenReturn(Optional.of(product));
        when(saveProductPort.saveProduct(any(Product.class))).thenReturn(product);
        when(productMapper.toResult(product)).thenReturn(mockResult);

        // Execute the service method to deactivate the product
        ProductResult result = service.execute(command);

        // Capture the saved product to inspect its state
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(saveProductPort).saveProduct(productCaptor.capture());

        // Verify that the product passed to the save port is marked as inactive
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.INACTIVE);

        // Verify that the returned result is exactly the mapped mock result
        assertThat(result).isSameAs(mockResult);
    }

    @Test
    void reactivatingSavesAnActiveProduct() {
        // Create an inactive product
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Product product = Product.builder().id(productId).sellerId(sellerId).status(ProductStatus.INACTIVE).build();
        SetProductActiveCommand command = new SetProductActiveCommand(productId, sellerId, true);

        // Prepare the mocked returned result
        ProductResult mockResult = ProductResult.builder().id(productId).status(ProductStatus.ACTIVE).build();

        // Simulate finding the product, saving it, and mapping the result
        when(loadProductPort.loadProduct(productId)).thenReturn(Optional.of(product));
        when(saveProductPort.saveProduct(any(Product.class))).thenReturn(product);
        when(productMapper.toResult(product)).thenReturn(mockResult);

        // Execute the service method to reactivate the product
        ProductResult result = service.execute(command);

        // Capture the saved product to inspect its state
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(saveProductPort).saveProduct(productCaptor.capture());

        // Verify that the product passed to the save port is marked as active
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(ProductStatus.ACTIVE);

        // Verify that the returned result is exactly the mapped mock result
        assertThat(result).isSameAs(mockResult);
    }

    @Test
    void productMapperMapsActiveStatusToResult() {
        // Obtain a real instance of the mapper
        ProductMapper realMapper = Mappers.getMapper(ProductMapper.class);

        // Create a domain product with the status set to ACTIVE
        Product product = Product.builder().sellerId(UUID.randomUUID()).status(ProductStatus.ACTIVE).build();

        // Map the product to a result DTO
        ProductResult result = realMapper.toResult(product);

        // Verify that the active status is successfully mapped to the result
        assertThat(result.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void productMapperMapsAnInactiveProductToAnInactiveResult() {
        // Obtain a real instance of the mapper
        ProductMapper realMapper = Mappers.getMapper(ProductMapper.class);

        // Create a domain product with the status set to INACTIVE
        Product product = Product.builder().sellerId(UUID.randomUUID()).status(ProductStatus.INACTIVE).build();

        // Map the product to a result DTO
        ProductResult result = realMapper.toResult(product);

        // Verify that the inactive status is successfully mapped to the result
        assertThat(result.status()).isEqualTo(ProductStatus.INACTIVE);
    }
}
