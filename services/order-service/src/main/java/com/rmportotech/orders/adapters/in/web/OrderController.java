package com.rmportotech.orders.adapters.in.web;

import com.rmportotech.orders.adapters.in.web.dto.CreateOrderRequest;
import com.rmportotech.orders.adapters.in.web.dto.CreateOrderResponse;
import com.rmportotech.orders.adapters.in.web.dto.OrderResponse;
import com.rmportotech.orders.application.usecase.CreateOrderUseCase;
import com.rmportotech.orders.application.usecase.GetOrderByIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Orders", description = "Order operations")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase, GetOrderByIdUseCase getOrderByIdUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderByIdUseCase = getOrderByIdUseCase;
    }

    @Operation(summary = "Create an order")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@RequestBody @Valid CreateOrderRequest request) {
        var order = createOrderUseCase.execute(request.totalAmount());
        return new CreateOrderResponse(order.getId(), order.getTotalAmount(), order.getStatus().name(), order.getCreatedAt());
    }

    @Operation(summary = "Get order by id")
    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable UUID id) {
        var order = getOrderByIdUseCase.execute(id);
        return new OrderResponse(order.getId(), order.getTotalAmount(), order.getStatus().name(), order.getCreatedAt());
    }
}