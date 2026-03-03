package com.rmportotech.orders.adapters.in.web;

import com.rmportotech.orders.adapters.in.web.dto.CreateOrderRequest;
import com.rmportotech.orders.adapters.in.web.dto.CreateOrderResponse;
import com.rmportotech.orders.application.usecase.CreateOrderUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@RequestBody @Valid CreateOrderRequest request) {
        var order = createOrderUseCase.execute(request.totalAmount());
        return new CreateOrderResponse(order.getId(), order.getTotalAmount(), order.getStatus().name(), order.getCreatedAt());
    }
}