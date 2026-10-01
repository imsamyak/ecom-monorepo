package com.ecom.product.adapter.in.web.controller;

import com.ecom.product.adapter.in.web.dto.request.SetProductActiveRequest;
import com.ecom.product.adapter.in.web.dto.response.ProductResponse;
import com.ecom.product.adapter.in.web.mapper.ProductWebMapper;
import com.ecom.product.adapter.in.web.security.SecurityConfig;
import com.ecom.product.domain.exception.ProductNotFoundException;
import com.ecom.product.domain.exception.ProductNotOwnedException;
import com.ecom.product.port.in.usecase.product.CreateProductUseCase;
import com.ecom.product.port.in.usecase.product.DeleteProductUseCase;
import com.ecom.product.port.in.usecase.product.SetProductActiveUseCase;
import com.ecom.product.port.in.usecase.product.UpdateProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.command.SetProductActiveCommand;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.shared.security.JwtAuthenticationFilter;
import com.ecom.shared.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class ProductActiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock existing use cases required by ProductController
    @MockBean
    private CreateProductUseCase createProductUseCase;

    @MockBean
    private UpdateProductUseCase updateProductUseCase;

    @MockBean
    private DeleteProductUseCase deleteProductUseCase;

    // Mock the new use case to be added
    @MockBean
    private SetProductActiveUseCase setProductActiveUseCase;

    // Mock the web mapper used by the controller
    @MockBean
    private ProductWebMapper productWebMapper;

    @Test
    void returns200AndUpdatedActiveStatusWhenSuccessful() throws Exception {
        // Setup identifiers for the test
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        
        // Setup the new request with active set to false
        SetProductActiveRequest request = new SetProductActiveRequest(false);
        SetProductActiveCommand command = new SetProductActiveCommand(productId, sellerId, false);
        
        // Setup the result from the use case
        ProductResult result = new ProductResult(
                productId, sellerId, "Test Product", "Test Description", 10.0,
                LocalDateTime.now(), LocalDateTime.now(), false
        );
        
        // Setup the mapped response
        ProductResponse response = new ProductResponse(
                productId, sellerId, "Test Product", "Test Description", 10.0,
                LocalDateTime.now(), LocalDateTime.now(), false
        );
        
        // Mock the mapper to convert the request to a command
        when(productWebMapper.toCommand(any(SetProductActiveRequest.class), eq(sellerId), eq(productId)))
                .thenReturn(command);
                
        // Mock the use case to process the command and return a result
        when(setProductActiveUseCase.setProductActive(command)).thenReturn(result);
        
        // Mock the mapper to convert the result to a response
        when(productWebMapper.toResponse(result)).thenReturn(response);

        // Perform a PATCH request authenticated as the seller
        mockMvc.perform(patch("/products/{productId}/active", productId)
                        .header("Authorization", "Bearer " + sellerId + ":SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Verify the response is 200 OK
                .andExpect(status().isOk())
                // Verify the active status matches the use case result
                .andExpect(jsonPath("$.active").value(false));
                
        // Verify the use case was called with the correct command containing the seller, product, and active flag
        verify(setProductActiveUseCase).setProductActive(command);
    }

    @Test
    void returns404WhenProductDoesNotExist() throws Exception {
        // Setup identifiers for the test
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        
        // Setup the new request
        SetProductActiveRequest request = new SetProductActiveRequest(true);
        SetProductActiveCommand command = new SetProductActiveCommand(productId, sellerId, true);
        
        // Mock the mapper to return the command
        when(productWebMapper.toCommand(any(SetProductActiveRequest.class), eq(sellerId), eq(productId)))
                .thenReturn(command);
                
        // Mock the use case to throw a ProductNotFoundException
        when(setProductActiveUseCase.setProductActive(command)).thenThrow(new ProductNotFoundException(productId));

        // Perform a PATCH request authenticated as the seller
        mockMvc.perform(patch("/products/{productId}/active", productId)
                        .header("Authorization", "Bearer " + sellerId + ":SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Verify the response is 404 Not Found
                .andExpect(status().isNotFound());
    }

    @Test
    void returns403WhenSellerDoesNotOwnProduct() throws Exception {
        // Setup identifiers for the test
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        
        // Setup the new request
        SetProductActiveRequest request = new SetProductActiveRequest(true);
        SetProductActiveCommand command = new SetProductActiveCommand(productId, sellerId, true);
        
        // Mock the mapper to return the command
        when(productWebMapper.toCommand(any(SetProductActiveRequest.class), eq(sellerId), eq(productId)))
                .thenReturn(command);
                
        // Mock the use case to throw a ProductNotOwnedException
        when(setProductActiveUseCase.setProductActive(command)).thenThrow(new ProductNotOwnedException(productId, sellerId));

        // Perform a PATCH request authenticated as the seller
        mockMvc.perform(patch("/products/{productId}/active", productId)
                        .header("Authorization", "Bearer " + sellerId + ":SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Verify the response is 403 Forbidden
                .andExpect(status().isForbidden());
    }
}
