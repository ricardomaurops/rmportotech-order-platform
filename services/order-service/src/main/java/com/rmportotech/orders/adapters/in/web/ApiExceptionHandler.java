package com.rmportotech.orders.adapters.in.web;

import com.rmportotech.orders.application.usecase.GetOrderByIdUseCase.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OrderNotFoundException.class)
    public ErrorResponse handle(OrderNotFoundException ex) {
        return new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage(), Instant.now());
    }

    public record ErrorResponse(String code, String message, Instant timestamp) {}
}