package com.adil.orderledger.service;

import com.adil.orderledger.dto.CreateProductRequest;
import com.adil.orderledger.dto.ProductResponse;
import com.adil.orderledger.dto.UpdateProductRequest;
import com.adil.orderledger.exception.ResourceNotFoundException;
import com.adil.orderledger.model.Product;
import com.adil.orderledger.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .unitPrice(request.unitPrice())
                .stockQuantity(request.stockQuantity())
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return mapToResponse(findProduct(id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with ID: " + id)
                );
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getVersion()
        );
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProduct(id);

        product.setName(request.name());
        product.setUnitPrice(request.unitPrice());
        product.setStockQuantity(request.stockQuantity());

        return mapToResponse(product);
    }
}