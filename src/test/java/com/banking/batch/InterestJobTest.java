package com.banking.batch;

import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.entity.AccountEntity;
import com.banking.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.batch.core.BatchStatus.COMPLETED;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = { "transaction-events" })

class InterestJobTest {

        @Autowired
        private JobLauncher jobLauncher;

        @Autowired
        private Job interestCalculationJob;

        @Autowired
        private AccountRepository accountRepository;

        @Test
        void interestJob_shouldCompleteSuccessfully() throws Exception {
                JobParameters params = new JobParametersBuilder()
                                .addLong("run.id", System.currentTimeMillis())
                                .toJobParameters();

                JobExecution execution = jobLauncher
                                .run(interestCalculationJob, params);

                assertThat(execution.getStatus()).isEqualTo(COMPLETED);
        }

        @Test
        void interestJob_shouldApplyInterestToSavingsAccounts() throws Exception {
                // ARRANGE — create a savings account with 1000 balance
                AccountEntity account = createSavingsAccount("1000.00");
                accountRepository.save(account);

                BigDecimal balanceBefore = account.getBalance();

                // ACT — run the job
                JobParameters params = new JobParametersBuilder()
                                .addLong("run.id", System.currentTimeMillis())
                                .toJobParameters();

                jobLauncher.run(interestCalculationJob, params);

                // ASSERT — balance should have increased
                AccountEntity updated = accountRepository
                                .findById(account.getAccountId()).get();

                assertThat(updated.getBalance())
                                .isGreaterThan(balanceBefore);
        }

        @Test
        void interestJob_shouldNotApplyInterestToCurrentAccounts() throws Exception {
                // ARRANGE — current account, not savings
                AccountEntity account = createCurrentAccount("1000.00");
                accountRepository.save(account);

                BigDecimal balanceBefore = account.getBalance();

                // ACT
                JobParameters params = new JobParametersBuilder()
                                .addLong("run.id", System.currentTimeMillis())
                                .toJobParameters();

                jobLauncher.run(interestCalculationJob, params);

                // ASSERT — balance should NOT change
                AccountEntity updated = accountRepository
                                .findById(account.getAccountId()).get();

                assertThat(updated.getBalance())
                                .isEqualByComparingTo(balanceBefore);
        }

        @Test
        void interestJob_shouldNotApplyInterestToBlockedAccounts() throws Exception {
                // ARRANGE — blocked savings account
                AccountEntity account = createSavingsAccount("1000.00");
                account.setAccountStatus(AccountStatus.BLOCKED);
                accountRepository.save(account);

                BigDecimal balanceBefore = account.getBalance();

                // ACT
                JobParameters params = new JobParametersBuilder()
                                .addLong("run.id", System.currentTimeMillis())
                                .toJobParameters();

                jobLauncher.run(interestCalculationJob, params);

                // ASSERT — blocked account should NOT get interest
                AccountEntity updated = accountRepository
                                .findById(account.getAccountId()).get();

                assertThat(updated.getBalance())
                                .isEqualByComparingTo(balanceBefore);
        }

        // ── helpers ──────────────────────────────────────────────────────────────

        private AccountEntity createSavingsAccount(String balance) {
                AccountEntity entity = new AccountEntity();
                entity.setAccountId(UUID.randomUUID().toString());
                entity.setAccountNumber(UUID.randomUUID().toString());
                entity.setOwnerName("Test User");
                entity.setAccountType(AccountType.SAVINGS);
                entity.setBalance(new BigDecimal(balance));
                entity.setAccountStatus(AccountStatus.ACTIVE);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setOwnerId("user-123");
                return entity;
        }

        private AccountEntity createCurrentAccount(String balance) {
                AccountEntity entity = createSavingsAccount(balance);
                entity.setAccountType(AccountType.CURRENT);
                return entity;
        }
}