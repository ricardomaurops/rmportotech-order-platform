package com.rmportotech.orders.adapters.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateOrderResponse(
        UUID id,
        BigDecimal totalAmount,
        String status,
        Instant createdAt
) {}