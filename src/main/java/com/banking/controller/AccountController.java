package com.banking.controller;

import com.banking.domain.Account;
import com.banking.dto.request.AmountRequest;
import com.banking.dto.request.CreateAccountRequest;
import com.banking.dto.request.TransferRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.dto.response.TransactionResponse;
import com.banking.dto.response.TransferResponse;
import com.banking.exception.AccountNotFoundException;
import com.banking.mapper.TransactionMapper;
import com.banking.security.Role;
import com.banking.security.UserEntity;
import com.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new bank account")
    public AccountResponse createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        return AccountResponse.from(
                accountService.createAccount(
                        request.ownerName(),
                        request.accountType(),
                        request.initialDeposit(),
                        currentUser.getId())); // ← JWT userId stored as ownerId
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    @PreAuthorize("isAuthenticated()")
    public AccountResponse getAccount(@PathVariable String id,
            @AuthenticationPrincipal UserEntity currentUser) {
        Account account = accountService.getAccount(id);
        verifyOwnership(account, currentUser);
        return AccountResponse.from(account);
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit money into account")
    @PreAuthorize("isAuthenticated()")
    public AccountResponse deposit(@PathVariable String id,
            @Valid @RequestBody AmountRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        Account account = accountService.getAccount(id);
        verifyOwnership(account, currentUser);
        return AccountResponse.from(accountService.deposit(id, request.amount()));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money from account")
    @PreAuthorize("isAuthenticated()")
    public AccountResponse withdraw(@PathVariable String id,
            @Valid @RequestBody AmountRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        Account account = accountService.getAccount(id);
        verifyOwnership(account, currentUser);
        return AccountResponse.from(accountService.withdraw(id, request.amount()));
    }

    @PostMapping("/{id}/transfer")
    @Operation(summary = "Transfer money to another account")
    @PreAuthorize("isAuthenticated()")
    public TransferResponse transfer(@PathVariable String id,
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        Account account = accountService.getAccount(id);
        verifyOwnership(account, currentUser);
        List<Account> result = accountService.transfer(id, request.toAccountId(), request.amount());
        return TransferResponse.of(
                AccountResponse.from(result.get(0)),
                AccountResponse.from(result.get(1)));
    }

    private void verifyOwnership(Account account, UserEntity currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // admins bypass ownership check
        }
        if (account.getOwnerId() == null
                || !account.getOwnerId().equals(currentUser.getId())) {
            throw new AccountNotFoundException(
                    "Account not found with id: " + account.getAccountId());
        }
    }

    @GetMapping("/{id}/transactions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get paginated transaction history")
    public Page<TransactionResponse> getTransactions(
            @PathVariable String id,
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserEntity currentUser) {
        Account account = accountService.getAccount(id);
        verifyOwnership(account, currentUser);
        return accountService.getTransactions(id, pageable)
                .map(entity -> TransactionResponse.from(
                        TransactionMapper.toDomain(entity)));
    }
}