package com.rmportotech.orders.application.usecase;

import com.rmportotech.orders.domain.model.Order;

public interface OutboxService {
    void enqueueOrderCreated(Order order);
}