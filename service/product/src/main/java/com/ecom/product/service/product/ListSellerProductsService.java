package com.ecom.product.service.product;

import com.ecom.product.port.in.usecase.product.ListSellerProductsUseCase;
import com.ecom.product.port.in.usecase.product.dto.query.ListSellerProductsQuery;
import com.ecom.product.port.in.usecase.product.dto.result.ProductResult;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ecom.product.service.product.mapper.ProductMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSellerProductsService implements ListSellerProductsUseCase {

    private final LoadProductPort loadProductPort;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResult> listSellerProducts(ListSellerProductsQuery query) {
        return loadProductPort.loadProductsBySeller(query.sellerId())
                .stream()
                .map(productMapper::toResult)
                .toList();
    }
}


