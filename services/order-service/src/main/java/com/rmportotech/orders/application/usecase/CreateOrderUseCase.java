package com.rmportotech.orders.application.usecase;

import com.rmportotech.orders.domain.model.Order;
import com.rmportotech.orders.domain.ports.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    public CreateOrderUseCase(OrderRepository orderRepository, OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
    }

    @Transactional
    public Order execute(BigDecimal totalAmount) {
        var order = orderRepository.save(Order.create(totalAmount));
        outboxService.enqueueOrderCreated(order);
        return order;
    }
}