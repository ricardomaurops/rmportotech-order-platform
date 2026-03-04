package com.rmportotech.payment.adapters.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static org.apache.kafka.common.header.Headers.*;

@Component
public class OrderEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);

    @KafkaListener(topics = "${rmpt.kafka.topic.orders}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(
            String payload,
            @Header(name = "kafka_receivedMessageKey", required = false) String key,
            Acknowledgment ack
    ) {
        try {
            log.info("PAYMENT received order event key={} payload={}", key, payload);

            // RMPT-21/22/23 entram depois (persistir + aprovar/rejeitar + idempotência)

            ack.acknowledge();
        } catch (Exception e) {
            log.error("PAYMENT failed processing key={} payload={}", key, payload, e);
            // não dá ack -> reprocessa
            throw e;
        }
    }
}