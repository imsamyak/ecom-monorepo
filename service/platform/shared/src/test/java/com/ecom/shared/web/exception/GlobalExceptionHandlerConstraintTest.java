package com.ecom.shared.web.exception;

import com.ecom.shared.exception.InvalidRequestException;
import com.ecom.shared.exception.ResourceNotFoundException;
import com.ecom.shared.exception.UnauthorizedAccessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
class GlobalExceptionHandlerConstraintTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // Bean whose only field breaks its own rule, to produce a real violation
    private static class Bean {
        @Size(min = 3, message = "too short")
        String title = "ab";
    }

    @Test
    void constraintViolationReturns400WithPathAndMessage() {
        // Produce a genuine violation with the real validator
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Bean>> violations = validator.validate(new Bean());

        // Hand it to the handler
        ResponseEntity<Object> response = handler.handleConstraintViolation(new ConstraintViolationException(violations));

        // The client gets 400 with "field: message" in the body
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("title: too short", body.get("message"));
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
    }

    @Test
    void constraintViolationWithNoViolationsFallsBackToGenericMessage() {
        // An exception carrying zero violations has nothing specific to report
        ResponseEntity<Object> response = handler.handleConstraintViolation(new ConstraintViolationException(Set.of()));

        // The handler falls back to a generic message
        assertEquals("Validation failed", ((Map<String, Object>) response.getBody()).get("message"));
    }

    @Test
    void methodArgumentNotValidWithNoFieldErrorsFallsBackToGenericMessage() {
        // Mock a binding failure that has no field errors
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(List.of());

        // Handle it
        ResponseEntity<Object> response = handler.handleValidationException(ex);

        // Still a 400 with the generic message
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", ((Map<String, Object>) response.getBody()).get("message"));
    }

    @Test
    void errorBodyHasExactlyTimestampStatusErrorAndMessage() {
        // Handle any exception to get a body
        ResponseEntity<Object> response = handler.handleResourceNotFound(new ResourceNotFoundException("nope"));

        // The body shape is part of the API contract, so pin the exact keys and the timestamp type
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Set.of("timestamp", "status", "error", "message"), body.keySet());
        assertInstanceOf(LocalDateTime.class, body.get("timestamp"));
        assertEquals("Not Found", body.get("error"));
    }

    @Test
    void eachExceptionTypeMapsToItsOwnStatus() {
        // Each domain exception type is translated to a different HTTP status
        assertEquals(HttpStatus.NOT_FOUND, handler.handleResourceNotFound(new ResourceNotFoundException("x")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, handler.handleUnauthorizedAccess(new UnauthorizedAccessException("x")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleInvalidRequest(new InvalidRequestException("x")).getStatusCode());
    }

    @Test
    void unauthorizedBodyCarries403ReasonPhrase() {
        // Handle an authorization failure
        Map<String, Object> body = (Map<String, Object>) handler
                .handleUnauthorizedAccess(new UnauthorizedAccessException("x")).getBody();

        // The body repeats the status code and its reason phrase, with a timestamp
        assertEquals(403, body.get("status"));
        assertEquals("Forbidden", body.get("error"));
        assertTrue(body.containsKey("timestamp"));
    }
}
