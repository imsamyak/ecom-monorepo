package com.ecom.shared.web.exception;

import com.ecom.shared.exception.InvalidRequestException;
import com.ecom.shared.exception.ResourceNotFoundException;
import com.ecom.shared.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleResourceNotFoundReturns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product not found");
        
        ResponseEntity<Object> response = handler.handleResourceNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Should return 404 status");
        
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Product not found", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleUnauthorizedAccessReturns403() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Not owned");
        
        ResponseEntity<Object> response = handler.handleUnauthorizedAccess(ex);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "Should return 403 status");
        
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(403, body.get("status"));
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Not owned", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleInvalidRequestReturns400() {
        InvalidRequestException ex = new InvalidRequestException("Bad data");
        
        ResponseEntity<Object> response = handler.handleInvalidRequest(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400 status");
        
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Bad data", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleValidationExceptionReturns400() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "price", "must be greater than zero");
        
        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        ResponseEntity<Object> response = handler.handleValidationException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return 400 status");
        
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body, "Response body should not be null");
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("price: must be greater than zero", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
    }
}

