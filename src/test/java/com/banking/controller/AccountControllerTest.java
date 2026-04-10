package com.banking.controller;

import com.banking.domain.Account;
import com.banking.domain.AccountStatus;
import com.banking.domain.AccountType;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.InvalidAmountException;
import com.banking.exception.SameAccountTransferException;
import com.banking.security.SecurityConfig;
import com.banking.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class) 
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    // Helper — creates a real Account domain object the controller can map
    private Account stubAccount(AccountType type, BigDecimal balance) {
        return new Account("John", type, balance);
    }

    @Nested
    @WithMockUser( roles = "CUSTOMER")
    class CreateAccount {

        @Test
        void shouldReturn201WhenAccountCreated() throws Exception {
            Account account = stubAccount(AccountType.SAVINGS, new BigDecimal("1000.00"));
            when(accountService.createAccount(any(), any(), any())).thenReturn(account);

            String body = objectMapper.writeValueAsString(
                    Map.of("ownerName", "John",
                           "accountType", "SAVINGS",
                           "initialDeposit", "1000.00"));

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ownerName").value("John"))
                    .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
        }

        @Test
        void shouldReturn400WhenOwnerNameIsBlank() throws Exception {
            String body = objectMapper.writeValueAsString(
                    Map.of("ownerName", "",
                           "accountType", "SAVINGS",
                           "initialDeposit", "1000.00"));

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        void shouldReturn400WhenInitialDepositIsZero() throws Exception {
            String body = objectMapper.writeValueAsString(
                    Map.of("ownerName", "John",
                           "accountType", "SAVINGS",
                           "initialDeposit", "0.00"));

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        void shouldReturn400WhenAccountTypeIsMissing() throws Exception {
            String body = "{\"ownerName\":\"John\",\"initialDeposit\":\"1000.00\"}";

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @WithMockUser( roles = "CUSTOMER")
    class GetAccount {

        @Test
        void shouldReturn200WhenAccountFound() throws Exception {
            Account account = stubAccount(AccountType.SAVINGS, new BigDecimal("1000.00"));
            when(accountService.getAccount(account.getAccountId())).thenReturn(account);

            mockMvc.perform(get("/api/accounts/" + account.getAccountId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ownerName").value("John"));
        }

        @Test
        void shouldReturn404WhenAccountNotFound() throws Exception {
            when(accountService.getAccount("bad-id"))
                    .thenThrow(new AccountNotFoundException("Account not found with id: bad-id"));

            mockMvc.perform(get("/api/accounts/bad-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Account Not Found"));
        }
    }

    @Nested
    @WithMockUser( roles = "CUSTOMER")
    class Deposit {

        @Test
        void shouldReturn200AfterDeposit() throws Exception {
            Account account = stubAccount(AccountType.SAVINGS, new BigDecimal("1000.00"));
            account.deposit(new BigDecimal("500.00"));
            when(accountService.deposit(anyString(), any())).thenReturn(account);

            String body = objectMapper.writeValueAsString(Map.of("amount", "500.00"));

            mockMvc.perform(post("/api/accounts/some-id/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1500.00));
        }

        @Test
        void shouldReturn400WhenDepositAmountIsZero() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("amount", "0.00"));

            mockMvc.perform(post("/api/accounts/some-id/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        void shouldReturn404WhenDepositingToNonExistentAccount() throws Exception {
            when(accountService.deposit(eq("bad-id"), any()))
                    .thenThrow(new AccountNotFoundException("Account not found with id: bad-id"));

            String body = objectMapper.writeValueAsString(Map.of("amount", "500.00"));

            mockMvc.perform(post("/api/accounts/bad-id/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @WithMockUser( roles = "CUSTOMER")
    class Withdraw {

        @Test
        void shouldReturn200AfterWithdrawal() throws Exception {
            Account account = stubAccount(AccountType.SAVINGS, new BigDecimal("1000.00"));
            account.withdraw(new BigDecimal("200.00"));
            when(accountService.withdraw(anyString(), any())).thenReturn(account);

            String body = objectMapper.writeValueAsString(Map.of("amount", "200.00"));

            mockMvc.perform(post("/api/accounts/some-id/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(800.00));
        }

        @Test
        void shouldReturn400WhenInsufficientBalance() throws Exception {
            when(accountService.withdraw(anyString(), any()))
                    .thenThrow(new InsufficientBalanceException("Insufficient balance. Available: 1000.00"));

            String body = objectMapper.writeValueAsString(Map.of("amount", "5000.00"));

            mockMvc.perform(post("/api/accounts/some-id/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Insufficient Balance"));
        }

        @Test
        void shouldReturn400WhenWithdrawAmountIsNegative() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("amount", "-100.00"));

            mockMvc.perform(post("/api/accounts/some-id/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }
    }

    @Nested
    @WithMockUser( roles = "CUSTOMER")
    class Transfer {

        @Test
        void shouldReturn200AfterTransfer() throws Exception {
            Account from = stubAccount(AccountType.SAVINGS, new BigDecimal("800.00"));
            Account to = stubAccount(AccountType.SAVINGS, new BigDecimal("700.00"));
            when(accountService.transfer(anyString(), anyString(), any()))
                    .thenReturn(List.of(from, to));

            String body = objectMapper.writeValueAsString(
                    Map.of("toAccountId", "target-id", "amount", "200.00"));

            mockMvc.perform(post("/api/accounts/some-id/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fromAccount").exists())
                    .andExpect(jsonPath("$.toAccount").exists());
        }

        @Test
        void shouldReturn400WhenAmountIsZero() throws Exception {
            String body = objectMapper.writeValueAsString(
                    Map.of("toAccountId", "target-id", "amount", "0.00"));

            mockMvc.perform(post("/api/accounts/some-id/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }

        @Test
        void shouldReturn400WhenSameAccount() throws Exception {
            when(accountService.transfer(anyString(), anyString(), any()))
                    .thenThrow(new SameAccountTransferException("Cannot transfer to the same account"));

            String body = objectMapper.writeValueAsString(
                    Map.of("toAccountId", "some-id", "amount", "200.00"));

            mockMvc.perform(post("/api/accounts/some-id/transfer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid Transfer"));
        }
    }
}