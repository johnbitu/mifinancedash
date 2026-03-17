package dev.project.finance.controllers;

import dev.project.finance.dtos.AccountSummary;
import dev.project.finance.dtos.CreateAccountRequest;
import dev.project.finance.dtos.UpdateAccountRequest;
import dev.project.finance.services.AccountService;
import dev.project.finance.configs.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<AccountSummary> criar(
            @RequestBody @Valid CreateAccountRequest request
    ) {
        Long userId = securityUtils.getUsuarioAutenticadoId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<AccountSummary>> listarPorUsuario() {
        Long userId = securityUtils.getUsuarioAutenticadoId();
        return ResponseEntity.ok(accountService.findAllByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountSummary> buscarPorId(@PathVariable Long id) {
        Long userId = securityUtils.getUsuarioAutenticadoId();
        return ResponseEntity.ok(accountService.findByIdAndUserId(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountSummary> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid UpdateAccountRequest request
    ) {
        Long userId = securityUtils.getUsuarioAutenticadoId();
        return ResponseEntity.ok(accountService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        Long userId = securityUtils.getUsuarioAutenticadoId();
        accountService.deactivate(id, userId);
        return ResponseEntity.noContent().build();
    }
}