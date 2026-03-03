package com.rmportotech.orders.infrastructure.config;

import com.rmportotech.orders.application.usecase.CreateOrderUseCase;
import com.rmportotech.orders.domain.ports.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository orderRepository) {
        return new CreateOrderUseCase(orderRepository);
    }
}