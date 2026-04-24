package com.banking.batch;

import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.entity.AccountEntity;
import com.banking.repository.AccountRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class InterestCalculationJobConfig {

    private final AccountRepository accountRepository;
    private final InterestItemProcessor processor;

    public InterestCalculationJobConfig(AccountRepository accountRepository,
            InterestItemProcessor processor) {
        this.accountRepository = accountRepository;
        this.processor = processor;
    }

    @Bean
    public RepositoryItemReader<AccountEntity> savingsAccountReader() {
        return new RepositoryItemReaderBuilder<AccountEntity>()
                .name("savingsAccountReader")
                .repository(accountRepository)
                .methodName("findByAccountTypeAndAccountStatus")
                .arguments(AccountType.SAVINGS, AccountStatus.ACTIVE)
                .sorts(Map.of("accountId", Sort.Direction.ASC))
                .pageSize(1000)
                .build();
    }

    @Bean
    public RepositoryItemWriter<AccountEntity> accountItemWriter() {
        return new RepositoryItemWriterBuilder<AccountEntity>()
                .repository(accountRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step interestCalculationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {

        return new StepBuilder("interestCalculationStep", jobRepository)
                .<AccountEntity, AccountEntity>chunk(1000, transactionManager)
                .reader(savingsAccountReader())
                .processor(processor)
                .writer(accountItemWriter())
                .build();
    }

    @Bean
    public Job interestCalculationJob(
            JobRepository jobRepository,
            Step interestCalculationStep) {

        return new JobBuilder("interestCalculationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(interestCalculationStep)
                .build();
    }
}