package com.adil.orderledger.service;

import com.adil.orderledger.dto.CreateOrderRequest;
import com.adil.orderledger.dto.OrderItemRequest;
import com.adil.orderledger.exception.InvalidCouponException;
import com.adil.orderledger.exception.ResourceNotFoundException;
import com.adil.orderledger.model.Coupon;
import com.adil.orderledger.model.Product;
import com.adil.orderledger.repository.CouponRepository;
import com.adil.orderledger.repository.OrderRepository;
import com.adil.orderledger.repository.OrderStatusHistoryRepository;
import com.adil.orderledger.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CouponIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository historyRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        orderRepository.deleteAll();
        couponRepository.deleteAll();
        productRepository.deleteAll();

        product = productRepository.saveAndFlush(
                Product.builder()
                        .name("Coupon Test Product")
                        .unitPrice(new BigDecimal("100.00"))
                        .stockQuantity(10)
                        .build()
        );
    }

    @Test
    void createOrder_activeCoupon_shouldApplyDiscountAndIncreaseUsageCount() {
        Coupon coupon = saveCoupon(
                "WELCOME10",
                10,
                LocalDateTime.now().plusDays(7),
                100,
                0,
                true
        );

        CreateOrderRequest request = createOrderRequest(
                2,
                "welcome10"
        );

        var response = orderService.createOrder(request);

        Coupon updatedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertThat(response.totalAmount())
                .isEqualByComparingTo(new BigDecimal("180.00"));

        assertThat(updatedCoupon.getCurrentUsageCount())
                .isEqualTo(1);

        assertThat(updatedProduct.getStockQuantity())
                .isEqualTo(8);

        assertThat(orderRepository.count())
                .isEqualTo(1);

        assertThat(historyRepository.count())
                .isEqualTo(1);
    }

    @Test
    void createOrder_inactiveCoupon_shouldThrowAndRollbackEverything() {
        Coupon coupon = saveCoupon(
                "INACTIVE10",
                10,
                LocalDateTime.now().plusDays(7),
                100,
                0,
                false
        );

        CreateOrderRequest request = createOrderRequest(
                2,
                coupon.getCode()
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("inactive");

        Product unchangedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        Coupon unchangedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        assertThat(unchangedProduct.getStockQuantity())
                .isEqualTo(10);

        assertThat(unchangedCoupon.getCurrentUsageCount())
                .isZero();

        assertThat(orderRepository.count())
                .isZero();

        assertThat(historyRepository.count())
                .isZero();
    }

    @Test
    void createOrder_expiredCoupon_shouldThrowAndRollbackEverything() {
        Coupon coupon = saveCoupon(
                "EXPIRED10",
                10,
                LocalDateTime.now().minusDays(1),
                100,
                0,
                true
        );

        CreateOrderRequest request = createOrderRequest(
                2,
                coupon.getCode()
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("expired");

        Product unchangedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        Coupon unchangedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        assertThat(unchangedProduct.getStockQuantity())
                .isEqualTo(10);

        assertThat(unchangedCoupon.getCurrentUsageCount())
                .isZero();

        assertThat(orderRepository.count())
                .isZero();

        assertThat(historyRepository.count())
                .isZero();
    }

    @Test
    void createOrder_couponUsageLimitReached_shouldThrowAndRollbackEverything() {
        Coupon coupon = saveCoupon(
                "LIMIT10",
                10,
                LocalDateTime.now().plusDays(7),
                1,
                1,
                true
        );

        CreateOrderRequest request = createOrderRequest(
                2,
                coupon.getCode()
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("usage limit");

        Product unchangedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        Coupon unchangedCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow();

        assertThat(unchangedProduct.getStockQuantity())
                .isEqualTo(10);

        assertThat(unchangedCoupon.getCurrentUsageCount())
                .isEqualTo(1);

        assertThat(orderRepository.count())
                .isZero();

        assertThat(historyRepository.count())
                .isZero();
    }

    private CreateOrderRequest createOrderRequest(
            int quantity,
            String couponCode
    ) {
        return new CreateOrderRequest(
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                quantity
                        )
                ),
                couponCode
        );
    }

    private Coupon saveCoupon(
            String code,
            int discountPercentage,
            LocalDateTime expirationDate,
            int maxUsageLimit,
            int currentUsageCount,
            boolean active
    ) {
        Coupon coupon = Coupon.builder()
                .code(code)
                .discountPercentage(discountPercentage)
                .expirationDate(expirationDate)
                .maxUsageLimit(maxUsageLimit)
                .currentUsageCount(currentUsageCount)
                .isActive(active)
                .build();

        return couponRepository.saveAndFlush(coupon);
    }

    @Test
    void createOrder_nonExistingCoupon_shouldThrowAndRollbackEverything() {
        CreateOrderRequest request = createOrderRequest(
                2,
                "NOT_FOUND"
        );

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Coupon not found");

        Product unchangedProduct = productRepository.findById(product.getId())
                .orElseThrow();

        assertThat(unchangedProduct.getStockQuantity())
                .isEqualTo(10);

        assertThat(orderRepository.count())
                .isZero();

        assertThat(historyRepository.count())
                .isZero();

        assertThat(couponRepository.count())
                .isZero();
    }
}