package com.adil.orderledger.controller;

import com.adil.orderledger.dto.*;
import com.adil.orderledger.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders Management", description = "Endpoints for placing, retrieving, and updating order status")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new order")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details and status by ID")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(id, request);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get order status change history")
    public List<OrderStatusHistoryResponse> getOrderStatusHistory(@PathVariable Long id) {
        return orderService.getOrderStatusHistory(id);
    }
}