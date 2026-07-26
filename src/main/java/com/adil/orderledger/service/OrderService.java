package com.adil.orderledger.service;

import com.adil.orderledger.dto.CreateOrderRequest;
import com.adil.orderledger.dto.OrderItemRequest;
import com.adil.orderledger.dto.OrderItemResponse;
import com.adil.orderledger.dto.OrderResponse;
import com.adil.orderledger.dto.OrderStatusHistoryResponse;
import com.adil.orderledger.dto.UpdateOrderStatusRequest;
import com.adil.orderledger.exception.DuplicateOrderItemException;
import com.adil.orderledger.exception.InsufficientStockException;
import com.adil.orderledger.exception.InvalidCouponException;
import com.adil.orderledger.exception.InvalidStateTransitionException;
import com.adil.orderledger.exception.ResourceNotFoundException;
import com.adil.orderledger.model.Coupon;
import com.adil.orderledger.model.Order;
import com.adil.orderledger.model.OrderItem;
import com.adil.orderledger.model.OrderStatus;
import com.adil.orderledger.model.OrderStatusHistory;
import com.adil.orderledger.model.Product;
import com.adil.orderledger.repository.CouponRepository;
import com.adil.orderledger.repository.OrderRepository;
import com.adil.orderledger.repository.OrderStatusHistoryRepository;
import com.adil.orderledger.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

        for (OrderItemRequest itemRequest : request.items()) {

            if (!productIds.add(itemRequest.productId())) {
                throw new DuplicateOrderItemException(
                        "Product cannot appear more than once in the same order. Product ID: "
                                + itemRequest.productId()
                );
            }

            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + itemRequest.productId()
                    ));

            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product ID: " + product.getId()
                                + ". Available: " + product.getStockQuantity()
                                + ", requested: " + itemRequest.quantity()
                );
            }

            product.setStockQuantity(
                    product.getStockQuantity() - itemRequest.quantity()
            );

            BigDecimal subtotal = product.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));

            runningTotal = runningTotal.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getUnitPrice())
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
        }

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            String normalizedCouponCode = request.couponCode()
                    .trim()
                    .toUpperCase();

            Coupon coupon = couponRepository.findByCode(normalizedCouponCode)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Coupon not found with code: " + normalizedCouponCode
                    ));

            if (!coupon.getIsActive()) {
                throw new InvalidCouponException(
                        "Coupon is inactive: " + normalizedCouponCode
                );
            }

            if (coupon.getExpirationDate() != null
                    && coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
                throw new InvalidCouponException(
                        "Coupon has expired: " + normalizedCouponCode
                );
            }

            if (coupon.getCurrentUsageCount() >= coupon.getMaxUsageLimit()) {
                throw new InvalidCouponException(
                        "Coupon usage limit exceeded for: " + normalizedCouponCode
                );
            }

            BigDecimal discountFactor = BigDecimal
                    .valueOf(100 - coupon.getDiscountPercentage())
                    .divide(
                            BigDecimal.valueOf(100),
                            4,
                            RoundingMode.HALF_UP
                    );

            runningTotal = runningTotal
                    .multiply(discountFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            coupon.setCurrentUsageCount(
                    coupon.getCurrentUsageCount() + 1
            );
        }

        order.setTotalAmount(runningTotal);

        Order savedOrder = orderRepository.save(order);

        recordStatusHistory(
                savedOrder.getId(),
                null,
                OrderStatus.CREATED,
                "Order Created"
        );

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId
                ));

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            UpdateOrderStatusRequest request
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + orderId
                ));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.newStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition order status from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();

                product.setStockQuantity(
                        product.getStockQuantity() + item.getQuantity()
                );
            }
        }

        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);

        recordStatusHistory(
                orderId,
                currentStatus,
                newStatus,
                request.reason()
        );

        return mapToOrderResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderStatusHistory(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException(
                    "Order not found with ID: " + orderId
            );
        }

        return historyRepository
                .findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(history -> new OrderStatusHistoryResponse(
                        history.getId(),
                        history.getOrderId(),
                        history.getPreviousStatus(),
                        history.getNewStatus(),
                        history.getReason(),
                        history.getCreatedAt()
                ))
                .toList();
    }

    private void recordStatusHistory(
            Long orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String reason
    ) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(orderId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();

        historyRepository.save(history);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
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