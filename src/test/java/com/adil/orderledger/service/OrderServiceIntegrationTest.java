package com.adil.orderledger.service;

import com.adil.orderledger.dto.CreateOrderRequest;
import com.adil.orderledger.dto.OrderItemRequest;
import com.adil.orderledger.exception.InsufficientStockException;
import com.adil.orderledger.model.Product;
import com.adil.orderledger.repository.OrderRepository;
import com.adil.orderledger.repository.OrderStatusHistoryRepository;
import com.adil.orderledger.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();

        product = Product.builder()
                .name("Test Product")
                .unitPrice(new BigDecimal("100.00"))
                .stockQuantity(5)
                .build();

        product = productRepository.saveAndFlush(product);
    }

    @Test
    void createOrder_success_shouldReduceStockAndCreateHistory() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(
                        new OrderItemRequest(product.getId(), 2)
                ),
                null
        );

        var response = orderService.createOrder(request);

        Product updatedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(response.totalAmount())
                .isEqualByComparingTo(new BigDecimal("200.00"));

        assertThat(updatedProduct.getStockQuantity())
                .isEqualTo(3);

        assertThat(orderRepository.count())
                .isEqualTo(1);

        assertThat(historyRepository.count())
                .isEqualTo(1);
    }

    @Test
    void createOrder_insufficientStock_shouldRollbackEverything() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(
                        new OrderItemRequest(product.getId(), 10)
                ),
                null
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class);

        Product unchangedProduct = productRepository
                .findById(product.getId())
                .orElseThrow();

        assertThat(unchangedProduct.getStockQuantity())
                .isEqualTo(5);

        assertThat(orderRepository.count())
                .isZero();

        assertThat(historyRepository.count())
                .isZero();
    }
}