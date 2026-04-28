package com.banking.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable event published to Kafka on every banking transaction.
 *
 * WHY THIS EXISTS:
 * Before Kafka, AccountService called NotificationService directly.
 * Every new requirement (audit, fraud, analytics) meant modifying
 * AccountService.
 *
 * Now AccountService publishes ONE event.
 * Any number of consumers can react — without touching AccountService.
 *
 * This is the Open/Closed Principle in action.
 */
public class TransactionEvent {

    public enum EventType {
        DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN
    }

    private final String eventId;
    private final String accountId;
    private final EventType eventType;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final LocalDateTime occurredAt;
    private final String description;

    public TransactionEvent(String eventId, String accountId, EventType eventType,
            BigDecimal amount, BigDecimal balanceAfter, String description) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.eventType = eventType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.occurredAt = LocalDateTime.now();
        this.description = description;
    }

    // Default constructor required by Jackson for deserialization
    public TransactionEvent() {
        this.eventId = null;
        this.accountId = null;
        this.eventType = null;
        this.amount = null;
        this.balanceAfter = null;
        this.occurredAt = null;
        this.description = null;
    }

    public String getEventId() {
        return eventId;
    }

    public String getAccountId() {
        return accountId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "TransactionEvent{eventId=" + eventId +
                ", accountId=" + accountId +
                ", type=" + eventType +
                ", amount=" + amount + "}";
    }
}