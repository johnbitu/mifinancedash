package dev.project.finance.controllers;

import dev.project.finance.dtos.CreateAccountRequest;
import dev.project.finance.models.Account;
import dev.project.finance.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid CreateAccountRequest request
    ) {
        Account account = accountService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping
    public ResponseEntity<List<Account>> findAllAccountsByUser(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<Account> accounts = accountService.findAllByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findAccountById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        Account account = accountService.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(account);
    }
}