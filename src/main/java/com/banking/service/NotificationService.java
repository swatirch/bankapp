package com.banking.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Async("notificationExecutor")
    public void sendDepositNotification(String accountId, BigDecimal amount) {
        logger.info("[{}] Deposit notification - account: {}, amount:{}",
                Thread.currentThread().getName(), accountId, amount);
    }

    @Async("notificationExecutor")
    public void sendWithdrawalNotification(String accountId,
            BigDecimal amount) {
        logger.info("[{}] Withdrawal notification — account: {}, amount: {}",
                Thread.currentThread().getName(), accountId, amount);
    }

    @Async("notificationExecutor")
    public void sendTransferNotification(String fromAccountId,
            String toAccountId,
            BigDecimal amount) {
        logger.info("[{}] Transfer notification — from: {}, to: {}, amount: {}",
                Thread.currentThread().getName(), fromAccountId, toAccountId, amount);
    }
}
