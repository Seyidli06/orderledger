package com.adil.orderledger.service;

import com.adil.orderledger.dto.*;
import com.adil.orderledger.exception.*;
import com.adil.orderledger.model.*;
import com.adil.orderledger.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal runningTotal = BigDecimal.ZERO;

        Set<Long> productIds = new HashSet<>();

        for (OrderItemRequest itemReq : request.items()) {

            if (!productIds.add(itemReq.productId())) {
                throw new DuplicateOrderItemException(
                        "Product cannot appear more than once in the same order. Product ID: "
                                + itemReq.productId()
                );
            }

            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + itemReq.productId()
                    ));


            // 3. Dynamic Price Calculation
            BigDecimal subtotal = product.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            runningTotal = runningTotal.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getUnitPrice())
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
        }

        // 4. Dynamic Coupon Processing & Validation
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(request.couponCode().trim().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with code: " + request.couponCode()));

            if (!coupon.getIsActive()) {
                throw new InvalidCouponException("Coupon is inactive: " + request.couponCode());
            }

            if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
                throw new InvalidCouponException("Coupon has expired: " + request.couponCode());
            }

            if (coupon.getCurrentUsageCount() >= coupon.getMaxUsageLimit()) {
                throw new InvalidCouponException("Coupon usage limit exceeded for: " + request.couponCode());
            }

            // Apply percentage discount: Subtotal - (Subtotal * (DiscountPercent / 100))
            BigDecimal discountFactor = BigDecimal.valueOf(100 - coupon.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            runningTotal = runningTotal.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);

            // Increment usage count (JPA dirty checking saves the updated count on commit)
            coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
        }

        order.setTotalAmount(runningTotal);
        Order savedOrder = orderRepository.save(order);

        // 5. Record Initial Immutable Audit Log
        recordStatusHistory(savedOrder.getId(), null, OrderStatus.CREATED, "Order Created");

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.newStatus();

        // 1. Enforce State Machine Transitions
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException("Cannot transition order status from " + currentStatus + " to " + newStatus);
        }

        // 2. Auto-restore Inventory on Cancellation
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // 3. Append Status Audit Log Record
        recordStatusHistory(orderId, currentStatus, newStatus, request.reason());

        return mapToOrderResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderStatusHistory(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }
        return historyRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(h -> new OrderStatusHistoryResponse(
                        h.getId(),
                        h.getOrderId(),
                        h.getPreviousStatus(),
                        h.getNewStatus(),
                        h.getReason(),
                        h.getCreatedAt()
                ))
                .toList();
    }

    private void recordStatusHistory(Long orderId, OrderStatus prevStatus, OrderStatus newStatus, String reason) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(orderId)
                .previousStatus(prevStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
        historyRepository.save(history);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}