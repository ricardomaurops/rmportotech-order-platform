package com.rmportotech.inventory.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Component
public class DynamoDbTableInitializer {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbTableInitializer.class);

    private final DynamoDbClient dynamoDbClient;

    @Value("${rmpt.dynamodb.table.stock-reservations}")
    private String stockReservationsTable;

    public DynamoDbTableInitializer(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @PostConstruct
    public void init() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(stockReservationsTable)
                    .build());

            log.info("DynamoDB table already exists: {}", stockReservationsTable);

        } catch (ResourceNotFoundException ex) {
            log.info("Creating DynamoDB table: {}", stockReservationsTable);

            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(stockReservationsTable)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("reservationId")
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("reservationId")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());

            log.info("DynamoDB table created: {}", stockReservationsTable);
        }
    }
}