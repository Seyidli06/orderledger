package com.adil.orderledger.repository;

import com.adil.orderledger.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}