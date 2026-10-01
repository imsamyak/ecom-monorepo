package com.ecom.product.adapter.in.web.dto.request;
import java.util.Map;
public record AddVariantRequest(
    Map<String, String> properties
) {}
