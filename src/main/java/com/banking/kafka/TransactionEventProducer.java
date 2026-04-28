package com.banking.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TransactionEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventProducer.class);
    static final String TOPIC = "banking.transactions";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, TransactionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(TransactionEvent event) {
        CompletableFuture<SendResult<String, TransactionEvent>> future = kafkaTemplate.send(TOPIC, event.getAccountId(),
                event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                logger.error("Failed to publish event {} for account {}: {}",
                        event.getEventId(), event.getAccountId(), ex.getMessage());
            } else {
                logger.info("Published {} event for account {} → partition={} offset={}",
                        event.getEventType(),
                        event.getAccountId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}