package com.rmportotech.orders.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(org.springframework.kafka.core.ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }
}