package dev.project.finance.controllers;

import dev.project.finance.configs.SecurityUtils;
import dev.project.finance.dtos.CreateRecurrenceRequest;
import dev.project.finance.dtos.RecurrenceHistoricoResponse;
import dev.project.finance.dtos.RecurrenceSummary;
import dev.project.finance.services.RecurrenceService;
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
@RequestMapping("/recurrences")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Recurrences")
public class RecurrenceController {

    private final RecurrenceService recurrenceService;
    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Cria recorrencia")
    public ResponseEntity<RecurrenceSummary> criar(@RequestBody @Valid CreateRecurrenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recurrenceService.create(request, securityUtils.getUsuarioAutenticado()));
    }

    @GetMapping
    @Operation(summary = "Lista recorrencias")
    public ResponseEntity<List<RecurrenceSummary>> listar(@RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(recurrenceService.listar(securityUtils.getUsuarioAutenticadoId(), ativo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca recorrencia por id")
    public ResponseEntity<RecurrenceSummary> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(recurrenceService.buscar(id, securityUtils.getUsuarioAutenticadoId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza recorrencia")
    public ResponseEntity<RecurrenceSummary> atualizar(@PathVariable Long id,
                                                       @RequestBody @Valid CreateRecurrenceRequest request) {
        return ResponseEntity.ok(recurrenceService.atualizar(id, securityUtils.getUsuarioAutenticadoId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa recorrencia")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        recurrenceService.desativar(id, securityUtils.getUsuarioAutenticadoId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/executar")
    @Operation(summary = "Executa recorrencia manualmente")
    public ResponseEntity<Void> executar(@PathVariable Long id) {
        recurrenceService.executarManual(id, securityUtils.getUsuarioAutenticadoId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Lista historico de transacoes da recorrencia")
    public ResponseEntity<RecurrenceHistoricoResponse> historico(@PathVariable Long id) {
        return ResponseEntity.ok(recurrenceService.historico(id, securityUtils.getUsuarioAutenticadoId(), transactionService));
    }
}
