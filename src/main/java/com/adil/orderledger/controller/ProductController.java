package com.adil.orderledger.controller;

import com.adil.orderledger.dto.*;
import com.adil.orderledger.model.Product;
import com.adil.orderledger.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products Catalog", description = "Endpoints for managing products catalog and inventory")
public class ProductController {



    private final ProductRepository productRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest req) {
        Product product = Product.builder()
                .name(req.name())
                .unitPrice(req.unitPrice())
                .stockQuantity(req.stockQuantity())
                .build();
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @GetMapping
    @Operation(summary = "List all products")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 1. ADDED: GET Product by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ProductResponse getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id));
        return mapToResponse(product);
    }

    // 2. Helper method to keep code DRY (Don't Repeat Yourself)
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getVersion()
        );
    }
}