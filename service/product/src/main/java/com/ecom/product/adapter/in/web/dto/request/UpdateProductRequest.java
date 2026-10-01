package com.ecom.product.adapter.in.web.dto.request;

public record UpdateProductRequest(
    String title,
    String description,
    double price
) {}
