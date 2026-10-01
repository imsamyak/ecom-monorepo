package com.ecom.product.service.product;

import com.ecom.product.domain.exception.ProductNotFoundException;

import com.ecom.product.port.in.usecase.product.GetProductUseCase;
import com.ecom.product.port.in.usecase.product.dto.query.GetProductQuery;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.product.service.product.mapper.ProductMapper;
import com.ecom.product.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor 
public class GetProductService implements GetProductUseCase {

    private final LoadProductPort loadProductPort;
    private final ProductMapper productMapper;

    @Override
    public ProductResult getProduct(GetProductQuery query) {
        Product product = loadProductPort.loadProduct(query.productId())
                .orElseThrow(() -> new ProductNotFoundException(query.productId()));

        return productMapper.toResult(product);
    }
}




