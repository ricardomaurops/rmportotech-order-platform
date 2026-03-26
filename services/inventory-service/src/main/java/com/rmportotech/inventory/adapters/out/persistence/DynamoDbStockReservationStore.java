package com.rmportotech.inventory.adapters.out.persistence;

import com.rmportotech.inventory.domain.model.StockReservation;
import com.rmportotech.inventory.domain.ports.StockReservationStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class DynamoDbStockReservationStore implements StockReservationStore {

    private final DynamoDbClient dynamoDbClient;

    @Value("${rmpt.dynamodb.table.stock-reservations}")
    private String stockReservationsTable;

    public DynamoDbStockReservationStore(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void save(StockReservation stockReservation) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("reservationId", AttributeValue.builder()
                .s(stockReservation.getReservationId().toString())
                .build());

        item.put("orderId", AttributeValue.builder()
                .s(stockReservation.getOrderId().toString())
                .build());

        item.put("createdAt", AttributeValue.builder()
                .s(stockReservation.getCreatedAt().toString())
                .build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(stockReservationsTable)
                .item(item)
                .build());
    }
}