package com.ecom.shared.usecase;

/**
 * Core interface for all Use Cases in the platform.
 * 
 * @param <C> The Command/Input type
 * @param <R> The Result/Output type
 */
public interface UseCase<C, R> {
    
    R execute(C command);
    
}


