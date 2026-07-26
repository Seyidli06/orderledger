package com.adil.orderledger.service;

import com.adil.orderledger.dto.CreateProductRequest;
import com.adil.orderledger.repository.OrderRepository;
import com.adil.orderledger.repository.OrderStatusHistoryRepository;
import com.adil.orderledger.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

import com.adil.orderledger.dto.UpdateProductRequest;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void createProduct_validRequest_shouldCreateProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "Mechanical Keyboard",
                new BigDecimal("120.50"),
                10
        );

        var response = productService.createProduct(request);

        assertThat(response.id())
                .isNotNull();

        assertThat(response.name())
                .isEqualTo("Mechanical Keyboard");

        assertThat(response.unitPrice())
                .isEqualByComparingTo(new BigDecimal("120.50"));

        assertThat(response.stockQuantity())
                .isEqualTo(10);

        assertThat(productRepository.count())
                .isEqualTo(1);

        var savedProduct = productRepository.findById(response.id())
                .orElseThrow();

        assertThat(savedProduct.getName())
                .isEqualTo("Mechanical Keyboard");

        assertThat(savedProduct.getUnitPrice())
                .isEqualByComparingTo(new BigDecimal("120.50"));

        assertThat(savedProduct.getStockQuantity())
                .isEqualTo(10);
    }

    @Test
    void updateProduct_existingProduct_shouldUpdateProduct() {
        var createdProduct = productService.createProduct(
                new CreateProductRequest(
                        "Old Product Name",
                        new BigDecimal("50.00"),
                        5
                )
        );

        var updateRequest = new UpdateProductRequest(
                "Updated Product Name",
                new BigDecimal("75.50"),
                12
        );

        var response = productService.updateProduct(
                createdProduct.id(),
                updateRequest
        );

        assertThat(response.id())
                .isEqualTo(createdProduct.id());

        assertThat(response.name())
                .isEqualTo("Updated Product Name");

        assertThat(response.unitPrice())
                .isEqualByComparingTo(new BigDecimal("75.50"));

        assertThat(response.stockQuantity())
                .isEqualTo(12);

        assertThat(productRepository.count())
                .isEqualTo(1);

        var updatedProduct = productRepository.findById(createdProduct.id())
                .orElseThrow();

        assertThat(updatedProduct.getName())
                .isEqualTo("Updated Product Name");

        assertThat(updatedProduct.getUnitPrice())
                .isEqualByComparingTo(new BigDecimal("75.50"));

        assertThat(updatedProduct.getStockQuantity())
                .isEqualTo(12);
    }
}