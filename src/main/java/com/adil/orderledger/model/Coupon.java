package com.adil.orderledger.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Version
    @Column(nullable = false)
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Integer discountPercentage;

    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private Integer maxUsageLimit;

    @Column(nullable = false)
    private Integer currentUsageCount;

    @Column(nullable = false)
    private Boolean isActive;
}