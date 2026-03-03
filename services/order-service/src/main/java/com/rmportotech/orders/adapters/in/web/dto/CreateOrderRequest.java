package com.rmportotech.orders.adapters.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotNull @DecimalMin("0.01") BigDecimal totalAmount
) {}