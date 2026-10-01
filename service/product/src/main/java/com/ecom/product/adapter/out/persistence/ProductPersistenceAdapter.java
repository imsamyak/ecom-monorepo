package com.ecom.product.adapter.out.persistence;

import com.ecom.product.adapter.out.persistence.repository.ProductRepository;
import com.ecom.product.domain.entity.Product;
import com.ecom.product.port.out.persistence.product.DeleteProductPort;
import com.ecom.product.port.out.persistence.product.LoadProductPort;
import com.ecom.product.port.out.persistence.product.SaveProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements SaveProductPort, LoadProductPort, DeleteProductPort {

    private final ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> loadProduct(UUID productId) {
        return productRepository.findById(productId);
    }

    @Override
    public void deleteProduct(UUID productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public List<Product> loadProductsBySeller(UUID sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
}
