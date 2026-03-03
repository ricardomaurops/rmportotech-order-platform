package com.rmportotech.orders.adapters.out.persistence;

import com.rmportotech.orders.domain.model.Order;
import com.rmportotech.orders.domain.model.OrderStatus;
import com.rmportotech.orders.domain.ports.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository jpa;

    public OrderRepositoryAdapter(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        var saved = jpa.save(new OrderEntity(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt()
        ));
        return new Order(saved.getId(), saved.getTotalAmount(), OrderStatus.valueOf(saved.getStatus()), saved.getCreatedAt());
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpa.findById(id)
                .map(e -> new Order(e.getId(), e.getTotalAmount(), OrderStatus.valueOf(e.getStatus()), e.getCreatedAt()));
    }
}