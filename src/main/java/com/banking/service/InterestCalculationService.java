package com.banking.service;

import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.entity.AccountEntity;
import com.banking.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InterestCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(InterestCalculationService.class);

    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.04"); // 4%
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal DAILY_RATE = ANNUAL_RATE.divide(DAYS_IN_YEAR, 10, RoundingMode.HALF_UP);

    private final AccountRepository accountRepository;

    public InterestCalculationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void calculateDailyInterest() {
        logger.info("Starting daily interest calculation");

        List<AccountEntity> savingsAccounts = accountRepository
                .findByAccountTypeAndAccountStatus(
                        AccountType.SAVINGS,
                        AccountStatus.ACTIVE);

        int count = 0;
        for (AccountEntity account : savingsAccounts) {
            BigDecimal interest = account.getBalance()
                    .multiply(DAILY_RATE)
                    .setScale(2, RoundingMode.HALF_UP);

            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                account.setBalance(account.getBalance().add(interest));
                accountRepository.save(account);
                count++;
            }
        }

        logger.info("Daily interest applied to {} accounts", count);
    }
}