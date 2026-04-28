package com.banking.service;

import com.banking.domain.Account;
import com.banking.domain.AccountType;
import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.AccountSummaryResponse;
import com.banking.entity.AccountEntity;
import com.banking.entity.TransactionEntity;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.SameAccountTransferException;
import com.banking.kafka.TransactionEvent;
import com.banking.kafka.TransactionEventProducer;
import com.banking.mapper.AccountMapper;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final TransactionEventProducer eventProducer;
    private final Executor queryExecutor;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            NotificationService notificationService,
            TransactionEventProducer eventProducer,
            Executor queryExecutor) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
        this.eventProducer = eventProducer;
        this.queryExecutor = queryExecutor;
    }

    @Transactional
    public Account createAccount(String ownerName, AccountType accountType,
            BigDecimal initialDeposit, String ownerId) {
        Account account = new Account(ownerName, accountType, initialDeposit);
        AccountEntity entity = AccountMapper.toEntity(account);
        entity.setOwnerId(ownerId);
        AccountEntity saved = accountRepository.save(entity);
        return AccountMapper.toDomain(saved);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountId")
    public Account deposit(String accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        account.deposit(amount);
        AccountEntity saved = accountRepository.save(AccountMapper.toEntity(account));
        Account result = AccountMapper.toDomain(saved);

        // Legacy notification (kept for backward compatibility)
        try {
            notificationService.sendDepositNotification(accountId, amount);
        } catch (Exception e) {
            logger.warn("Failed to send deposit notification for account {}: {}",
                    accountId, e.getMessage());
        }

        // Kafka event — decoupled, async, never blocks the transaction
        eventProducer.publish(new TransactionEvent(
                UUID.randomUUID().toString(),
                accountId,
                TransactionEvent.EventType.DEPOSIT,
                amount,
                result.getBalance(),
                "Deposit of " + amount));

        return result;
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountId")
    public Account withdraw(String accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        account.withdraw(amount);
        AccountEntity saved = accountRepository.save(AccountMapper.toEntity(account));
        Account result = AccountMapper.toDomain(saved);

        try {
            notificationService.sendWithdrawalNotification(accountId, amount);
        } catch (Exception e) {
            logger.warn("Failed to send withdrawal notification for account {}: {}",
                    accountId, e.getMessage());
        }

        eventProducer.publish(new TransactionEvent(
                UUID.randomUUID().toString(),
                accountId,
                TransactionEvent.EventType.WITHDRAWAL,
                amount,
                result.getBalance(),
                "Withdrawal of " + amount));

        return result;
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public List<Account> transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new SameAccountTransferException("Cannot transfer to the same account");
        }
        Account fromAccount = findAccountById(fromAccountId);
        Account toAccount = findAccountById(toAccountId);

        fromAccount.transferOut(amount);
        toAccount.transferIn(amount);

        AccountEntity savedFrom = accountRepository.save(AccountMapper.toEntity(fromAccount));
        AccountEntity savedTo = accountRepository.save(AccountMapper.toEntity(toAccount));

        Account resultFrom = AccountMapper.toDomain(savedFrom);
        Account resultTo = AccountMapper.toDomain(savedTo);

        try {
            notificationService.sendTransferNotification(fromAccountId, toAccountId, amount);
        } catch (Exception e) {
            logger.warn("Failed to send transfer notification for accounts {} -> {}: {}",
                    fromAccountId, toAccountId, e.getMessage());
        }

        // Two events — one per account (ordering guaranteed per account by Kafka key)
        eventProducer.publish(new TransactionEvent(
                UUID.randomUUID().toString(),
                fromAccountId,
                TransactionEvent.EventType.TRANSFER_OUT,
                amount,
                resultFrom.getBalance(),
                "Transfer to " + toAccountId));
        eventProducer.publish(new TransactionEvent(
                UUID.randomUUID().toString(),
                toAccountId,
                TransactionEvent.EventType.TRANSFER_IN,
                amount,
                resultTo.getBalance(),
                "Transfer from " + fromAccountId));

        return List.of(resultFrom, resultTo);
    }

    @Cacheable(value = "accounts", key = "#accountId")
    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        return findAccountById(accountId);
    }

    private Account findAccountById(String accountId) {
        return accountRepository.findById(accountId)
                .map(AccountMapper::toDomain)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + accountId));
    }

    @Transactional(readOnly = true)
    public Page<TransactionEntity> getTransactions(String accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException("Account not found with id: " + accountId);
        }
        return transactionRepository.findByAccount_AccountId(accountId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(AccountMapper::toDomain)
                .map(AccountResponse::from);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountId")
    public void blockAccount(String accountId) {
        Account account = findAccountById(accountId);
        account.block();
        accountRepository.save(AccountMapper.toEntity(account));
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountId")
    public void unblockAccount(String accountId) {
        Account account = findAccountById(accountId);
        account.activate();
        accountRepository.save(AccountMapper.toEntity(account));
    }

    public List<AccountSummaryResponse> getAllAccountsWithStats() {
        List<AccountEntity> accounts = accountRepository.findAll();
        List<CompletableFuture<AccountSummaryResponse>> futures = accounts
                .stream()
                .map(entity -> CompletableFuture
                        .supplyAsync(() -> {
                            long count = transactionRepository.countByAccount_AccountId(entity.getAccountId());
                            return AccountSummaryResponse.from(
                                    AccountResponse.from(AccountMapper.toDomain(entity)), count);
                        }, queryExecutor)
                        .exceptionally(ex -> {
                            logger.warn("Failed to fetch stats for account {}: {}",
                                    entity.getAccountId(), ex.getMessage());
                            return AccountSummaryResponse.from(
                                    AccountResponse.from(AccountMapper.toDomain(entity)), 0L);
                        }))
                .toList();
        return futures.stream().map(CompletableFuture::join).toList();
    }
}
