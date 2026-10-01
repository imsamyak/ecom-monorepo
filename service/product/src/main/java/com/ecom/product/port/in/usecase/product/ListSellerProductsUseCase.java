package com.ecom.product.port.in.usecase.product;

import com.ecom.product.port.in.usecase.product.dto.query.ListSellerProductsQuery;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;

import java.util.List;

public interface ListSellerProductsUseCase {
    List<ProductResult> listSellerProducts(ListSellerProductsQuery query);
}

