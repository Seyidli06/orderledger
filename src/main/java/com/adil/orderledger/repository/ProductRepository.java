package com.adil.orderledger.repository;

import com.adil.orderledger.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}