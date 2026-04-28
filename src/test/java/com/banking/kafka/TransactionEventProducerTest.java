package com.banking.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test — no Spring context, no real Kafka.
 * KafkaTemplate is mocked. We verify:
 * - correct topic is used
 * - accountId is the message key (ordering guarantee)
 * - publish never throws even if Kafka is down
 */
@ExtendWith(MockitoExtension.class)
class TransactionEventProducerTest {

    @Mock
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @InjectMocks
    private TransactionEventProducer producer;

    @Test
    void publish_sendsToCorrectTopic() {
        // ARRANGE
        TransactionEvent event = new TransactionEvent(
                "evt-1", "acc-123", TransactionEvent.EventType.DEPOSIT,
                BigDecimal.valueOf(500), BigDecimal.valueOf(1500), "Deposit");

        CompletableFuture<SendResult<String, TransactionEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // ACT
        producer.publish(event);

        // ASSERT — correct topic
        verify(kafkaTemplate).send(
                eq(TransactionEventProducer.TOPIC),
                eq("acc-123"),      // key = accountId → ordering guarantee
                eq(event)
        );
    }

    @Test
    void publish_usesAccountIdAsKey_guaranteesOrdering() {
        // ARRANGE
        TransactionEvent event = new TransactionEvent(
                "evt-2", "acc-456", TransactionEvent.EventType.WITHDRAWAL,
                BigDecimal.valueOf(100), BigDecimal.valueOf(900), "Withdrawal");

        CompletableFuture<SendResult<String, TransactionEvent>> future =
                CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // ACT
        producer.publish(event);

        // ASSERT — key must be accountId, not eventId or something else
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(any(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).isEqualTo("acc-456");
    }

    @Test
    void publish_doesNotThrow_whenKafkaIsDown() {
        // ARRANGE — simulate Kafka failure
        TransactionEvent event = new TransactionEvent(
                "evt-3", "acc-789", TransactionEvent.EventType.TRANSFER_OUT,
                BigDecimal.valueOf(200), BigDecimal.valueOf(800), "Transfer");

        CompletableFuture<SendResult<String, TransactionEvent>> failedFuture =
                CompletableFuture.failedFuture(new RuntimeException("Kafka broker unavailable"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        // ACT + ASSERT — must NOT throw; a failed Kafka publish must never rollback a DB transaction
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> producer.publish(event));
    }
}
