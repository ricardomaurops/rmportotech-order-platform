package com.rmportotech.orders.domain.ports;

import com.rmportotech.orders.domain.model.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
}