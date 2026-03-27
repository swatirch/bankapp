package com.banking.controller;

import com.banking.domain.Account;
import com.banking.dto.request.AmountRequest;
import com.banking.dto.request.CreateAccountRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/accounts")
@Tag(name="Account Management",description = "APIs for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService=accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new bank account", description = "Creates a new bank account with owner name, account type and initial deposit")
    @ApiResponse(responseCode = "201", description = "Account created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request){
        return AccountResponse.from(
                accountService.createAccount(
                        request.ownerName(),
                        request.accountType(),
                        request.initialDeposit()
                )
        );    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    @ApiResponse(responseCode = "200", description = "Account found successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public AccountResponse getAccount(@PathVariable String id) {
        return AccountResponse.from(
                accountService.getAccount(id)
        );
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Deposit money into account")
    @ApiResponse(responseCode = "200", description = "Deposit successful")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "400", description = "Invalid deposit amount")
    public AccountResponse deposit(@PathVariable String id,
                           @RequestBody AmountRequest request) {
        return AccountResponse.from(
                accountService.deposit(id, request.amount())
        );
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money from account")
    @ApiResponse(responseCode = "200", description = "Withdrawal successful")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "400", description = "Insufficient balance or invalid amount")
    public AccountResponse withdraw(@PathVariable String id,
                            @RequestBody AmountRequest request) {
        return AccountResponse.from(
                accountService.withdraw(id, request.amount())
        );
    }
}
