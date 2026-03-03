package com.rmportotech.orders.application.usecase;

import com.rmportotech.orders.domain.model.Order;
import com.rmportotech.orders.domain.ports.OrderRepository;

import java.math.BigDecimal;

public class CreateOrderUseCase {
    private final OrderRepository orderRepository;

    public CreateOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order execute(BigDecimal totalAmount) {
        return orderRepository.save(Order.create(totalAmount));
    }
}