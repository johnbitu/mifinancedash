package dev.project.finance.controllers;

import dev.project.finance.configs.SecurityUtils;
import dev.project.finance.dtos.CreateTransactionRequest;
import dev.project.finance.dtos.TransactionSummary;
import dev.project.finance.models.User;
import dev.project.finance.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Cria transacao")
    public ResponseEntity<TransactionSummary> criar(
            @RequestBody @Valid CreateTransactionRequest request
    ) {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(request, usuario));
    }

    @GetMapping
    @Operation(summary = "Lista transacoes")
    public ResponseEntity<List<TransactionSummary>> listar() {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.ok(transactionService.findAllByUsuario(usuario));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca transacao por id")
    public ResponseEntity<TransactionSummary> buscarPorId(@PathVariable Long id) {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.ok(transactionService.findByIdAndUsuario(id, usuario));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza transacao")
    public ResponseEntity<TransactionSummary> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid CreateTransactionRequest request
    ) {
        User usuario = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.ok(transactionService.update(id, request, usuario));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui transacao")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        User usuario = securityUtils.getUsuarioAutenticado();
        transactionService.delete(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
