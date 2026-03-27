package com.banking.domain;

import com.banking.exception.AccountStatusException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAccountException;
import com.banking.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Nested
    class SavingsAccountTest {

        private Account account;

        @BeforeEach
        void setUp() {
            account = new Account("John", AccountType.SAVINGS,
                    new BigDecimal("1000.00"));
        }

        @Test
        void shouldCreateSavingsAccountWithCorrectDetails() {
            assertEquals("John", account.getOwnerName());
            assertEquals(AccountType.SAVINGS, account.getAccountType());
            assertEquals(AccountStatus.ACTIVE, account.getAccountStatus());
            assertEquals(new BigDecimal("1000.00"), account.getBalance());
            assertNotNull(account.getAccountId());
            assertNotNull(account.getAccountNumber());
            assertNotNull(account.getCreatedAt());
        }

        @Test
        void shouldAllowOpeningSavingsAccountWithMinimumOneRupee() {
            Account minAccount = new Account("John", AccountType.SAVINGS,
                    new BigDecimal("1.00"));
            assertEquals(new BigDecimal("1.00"), minAccount.getBalance());
        }

        @Test
        void shouldNotAllowOpeningSavingsAccountWithZeroDeposit() {
            assertThrows(InvalidAmountException.class, () ->
                    new Account("John", AccountType.SAVINGS, BigDecimal.ZERO)
            );
        }

        @Test
        void shouldDepositMoney() {
            account.deposit(new BigDecimal("500.00"));
            assertEquals(new BigDecimal("1500.00"), account.getBalance());
        }

        @Test
        void shouldWithdrawMoney() {
            account.withdraw(new BigDecimal("200.00"));
            assertEquals(new BigDecimal("800.00"), account.getBalance());
        }

        @Test
        void shouldThrowExceptionWhenInsufficientBalance() {
            BigDecimal amount = new BigDecimal("2000.00");
            assertThrows(InsufficientBalanceException.class, () ->
                    account.withdraw(amount)
            );
        }

        @Test
        void shouldAllowWithdrawingExactBalance() {        // ← ADD HERE
            account.withdraw(new BigDecimal("1000.00"));
            assertEquals(new BigDecimal("0.00"), account.getBalance());
        }

        @Test
        void shouldThrowExceptionWhenTransactingOnBlockedAccount() {
            account.block();
            BigDecimal amount = new BigDecimal("500.00");
            assertThrows(AccountStatusException.class, () ->
                    account.deposit(amount)
            );
        }

        @Test
        void shouldThrowExceptionWhenTransactingOnInactiveAccount() {  // ← ADD HERE
            account.deactivate();
            BigDecimal amount = new BigDecimal("500.00");
            assertThrows(AccountStatusException.class, () ->
                    account.deposit(amount)
            );
        }

        @Test
        void shouldRecordInitialDepositAsFirstTransaction() {
            assertEquals(1, account.getTransactions().size());
            assertEquals(TransactionType.DEPOSIT,
                    account.getTransactions().get(0).getType());
            assertEquals("Initial deposit",
                    account.getTransactions().get(0).getDescription());
        }

        @Test
        void shouldRecordTransactionAfterDeposit() {
            account.deposit(new BigDecimal("500.00"));
            assertEquals(2, account.getTransactions().size());
            assertEquals(TransactionType.DEPOSIT,
                    account.getTransactions().get(1).getType());
        }

        @Test
        void shouldRecordTransactionAfterWithdrawal() {
            account.withdraw(new BigDecimal("200.00"));
            assertEquals(2, account.getTransactions().size());
            assertEquals(TransactionType.WITHDRAWAL,
                    account.getTransactions().get(1).getType());
        }


    }

    @Nested
    class CurrentAccountTest {

        @Test
        void shouldCreateCurrentAccountWithMinimumDeposit() {
            Account account = new Account("Business Ltd",
                    AccountType.CURRENT,
                    new BigDecimal("10000.00"));
            assertEquals(new BigDecimal("10000.00"), account.getBalance());
        }

        @Test
        void shouldNotAllowOpeningCurrentAccountBelowMinimum() {
            BigDecimal amount = new BigDecimal("5000.00");
            assertThrows(InvalidAmountException.class, () ->
                    new Account("Business Ltd", AccountType.CURRENT,
                            amount)
            );
        }
    }

    @Nested
    class FixedDepositAccountTest {

        @Test
        void shouldCreateFixedDepositAccountWithMinimumDeposit() {
            Account account = new Account("John",
                    AccountType.FIXED_DEPOSIT,
                    new BigDecimal("1000.00"));
            assertEquals(new BigDecimal("1000.00"), account.getBalance());
        }

        @Test
        void shouldNotAllowOpeningFixedDepositAccountBelowMinimum() {
            BigDecimal amount = new BigDecimal("500.00");
            assertThrows(InvalidAmountException.class, () ->
                    new Account("John", AccountType.FIXED_DEPOSIT,
                            amount)
            );
        }
        @Test
        void shouldNotAllowWithdrawalFromFixedDepositAccount() {
            Account fdAccount = new Account("John", AccountType.FIXED_DEPOSIT,
                    new BigDecimal("1000.00"));
            BigDecimal amount = new BigDecimal("500.00");
            assertThrows(AccountStatusException.class, () ->
                    fdAccount.withdraw(amount)
            );
        }
    }

    @Nested
    class AccountValidationTest {

        @Test
        void shouldThrowExceptionWhenNameIsNull() {
            BigDecimal amount = new BigDecimal("1000.00");
            assertThrows(InvalidAccountException.class, () ->
                    new Account(null, AccountType.SAVINGS,
                           amount)
            );
        }

        @Test
        void shouldThrowExceptionWhenNameIsBlank() {
            BigDecimal amount = new BigDecimal("1000.00");
            assertThrows(InvalidAccountException.class, () ->
                    new Account("   ", AccountType.SAVINGS,
                           amount)
            );
        }

        @Test
        void shouldThrowExceptionWhenDepositingZero() {
            Account account = new Account("John", AccountType.SAVINGS,
                    new BigDecimal("1000.00"));
            assertThrows(InvalidAmountException.class, () ->
                    account.deposit(BigDecimal.ZERO)
            );
        }

        @Test
        void shouldThrowExceptionWhenDepositingNegativeAmount() {
            Account account = new Account("John", AccountType.SAVINGS,
                    new BigDecimal("1000.00"));
            BigDecimal amount = new BigDecimal("-100.00");
            assertThrows(InvalidAmountException.class, () ->
                    account.deposit(amount)
            );
        }
    }
}
