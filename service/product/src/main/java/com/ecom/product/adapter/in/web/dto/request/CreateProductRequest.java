package com.ecom.product.adapter.in.web.dto.request;

public record CreateProductRequest(
    String title,
    String description,
    double price
) {}
