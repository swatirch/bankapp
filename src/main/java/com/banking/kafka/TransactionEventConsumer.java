package com.banking.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Audit log consumer — listens to banking.transactions topic.
 *
 * This is where the power of Kafka shows:
 * AccountService has ZERO knowledge this class exists.
 * Tomorrow you add FraudDetectionConsumer the same way — zero changes to
 * AccountService.
 *
 * In production this would write to an audit_log table.
 * Here we log it clearly so you can see it working.
 */
@Service
public class TransactionEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TransactionEventConsumer.class);

    @KafkaListener(topics = TransactionEventProducer.TOPIC, groupId = "banking-audit-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleTransactionEvent(
            @Payload TransactionEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        logger.info("[AUDIT] {} | account={} | amount={} | balanceAfter={} | partition={} | offset={}",
                event.getEventType(),
                event.getAccountId(),
                event.getAmount(),
                event.getBalanceAfter(),
                partition,
                offset);

        // In production: auditRepository.save(new AuditLogEntry(event));
    }
}
