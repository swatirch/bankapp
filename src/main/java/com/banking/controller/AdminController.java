package com.banking.controller;

import com.banking.dto.response.AccountResponse;
import com.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only account management")
public class AdminController {

    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    @Operation(summary = "List all accounts (ADMIN only)")
    public Page<AccountResponse> getAllAccounts(
            @PageableDefault(size = 20) Pageable pageable) {
        return accountService.getAllAccounts(pageable);
    }

    @PatchMapping("/accounts/{id}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Block an account (ADMIN only)")
    public void blockAccount(@PathVariable String id) {
        accountService.blockAccount(id);
    }

    @PatchMapping("/accounts/{id}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unblock an account (ADMIN only)")
    public void unblockAccount(@PathVariable String id) {
        accountService.unblockAccount(id);
    }
}