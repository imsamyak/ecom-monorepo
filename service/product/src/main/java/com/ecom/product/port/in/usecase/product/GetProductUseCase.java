package com.ecom.product.port.in.usecase.product;

import com.ecom.product.port.in.usecase.product.dto.query.GetProductQuery;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;

public interface GetProductUseCase {
    ProductResult getProduct(GetProductQuery query);
}

