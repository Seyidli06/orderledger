package com.adil.orderledger.controller;

import com.adil.orderledger.dto.*;
import com.adil.orderledger.model.Product;
import com.adil.orderledger.repository.ProductRepository;
import com.adil.orderledger.service.ProductService;
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



    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.createProduct(request);
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
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
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }
}