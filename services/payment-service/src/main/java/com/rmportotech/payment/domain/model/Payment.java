package com.rmportotech.payment.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class Payment {

    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant createdAt;

    // construtor, getters
}