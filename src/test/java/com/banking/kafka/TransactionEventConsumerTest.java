package com.banking.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit test for TransactionEventConsumer.
 *
 * We verify the consumer handles all event types without throwing.
 * Integration test with real Kafka topic is in KafkaIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class TransactionEventConsumerTest {

    private final TransactionEventConsumer consumer = new TransactionEventConsumer();

    @Test
    void handleDepositEvent_doesNotThrow() {
        TransactionEvent event = new TransactionEvent(
                "evt-1", "acc-123", TransactionEvent.EventType.DEPOSIT,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(2000), "Deposit");

        assertDoesNotThrow(() -> consumer.handleTransactionEvent(event, 0, 0L));
    }

    @Test
    void handleWithdrawalEvent_doesNotThrow() {
        TransactionEvent event = new TransactionEvent(
                "evt-2", "acc-123", TransactionEvent.EventType.WITHDRAWAL,
                BigDecimal.valueOf(500), BigDecimal.valueOf(1500), "Withdrawal");

        assertDoesNotThrow(() -> consumer.handleTransactionEvent(event, 0, 1L));
    }

    @Test
    void handleTransferEvent_doesNotThrow() {
        TransactionEvent event = new TransactionEvent(
                "evt-3", "acc-456", TransactionEvent.EventType.TRANSFER_OUT,
                BigDecimal.valueOf(300), BigDecimal.valueOf(700), "Transfer");

        assertDoesNotThrow(() -> consumer.handleTransactionEvent(event, 0, 2L));
    }

    @Test
    void handleTransferInEvent_doesNotThrow() {
        TransactionEvent event = new TransactionEvent(
                "evt-4", "acc-789", TransactionEvent.EventType.TRANSFER_IN,
                BigDecimal.valueOf(300), BigDecimal.valueOf(1300), "Transfer received");

        assertDoesNotThrow(() -> consumer.handleTransactionEvent(event, 1, 0L));
    }
}
