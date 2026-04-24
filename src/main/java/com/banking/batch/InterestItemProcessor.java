package com.banking.batch;

import com.banking.entity.AccountEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InterestItemProcessor
        implements ItemProcessor<AccountEntity, AccountEntity> {

    private static final Logger logger = LoggerFactory.getLogger(InterestItemProcessor.class);

    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.04");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal DAILY_RATE = ANNUAL_RATE.divide(DAYS_IN_YEAR, 10, RoundingMode.HALF_UP);

    @Override
    public AccountEntity process(AccountEntity account) {
        BigDecimal interest = account.getBalance()
                .multiply(DAILY_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // returning null → Spring Batch skips this item
        if (interest.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        account.setBalance(account.getBalance().add(interest));

        logger.debug("Interest applied to account {}: +{}",
                account.getAccountId(), interest);

        return account;
    }
}