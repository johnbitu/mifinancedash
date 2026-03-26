package dev.project.finance.controllers;

import dev.project.finance.configs.SecurityUtils;
import dev.project.finance.dtos.CardInvoiceSummary;
import dev.project.finance.dtos.CardSummary;
import dev.project.finance.dtos.CreateCardRequest;
import dev.project.finance.dtos.UpdateCardInvoiceRequest;
import dev.project.finance.models.User;
import dev.project.finance.services.CardService;
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
@RequestMapping("/cards")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Cards")
public class CardController {

    private final CardService cardService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Cria cartao")
    public ResponseEntity<CardSummary> criar(@RequestBody @Valid CreateCardRequest request) {
        User user = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Lista cartoes")
    public ResponseEntity<List<CardSummary>> listar() {
        return ResponseEntity.ok(cardService.listar(securityUtils.getUsuarioAutenticadoId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca cartao por id")
    public ResponseEntity<CardSummary> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.buscar(id, securityUtils.getUsuarioAutenticadoId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cartao")
    public ResponseEntity<CardSummary> atualizar(@PathVariable Long id, @RequestBody @Valid CreateCardRequest request) {
        return ResponseEntity.ok(cardService.atualizar(id, securityUtils.getUsuarioAutenticadoId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa cartao")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        cardService.desativar(id, securityUtils.getUsuarioAutenticadoId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/invoices")
    @Operation(summary = "Lista faturas do cartao")
    public ResponseEntity<List<CardInvoiceSummary>> listarFaturas(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.listarFaturas(id, securityUtils.getUsuarioAutenticadoId()));
    }

    @GetMapping("/{id}/invoices/{invoiceId}")
    @Operation(summary = "Busca fatura do cartao")
    public ResponseEntity<CardInvoiceSummary> buscarFatura(@PathVariable Long id, @PathVariable Long invoiceId) {
        return ResponseEntity.ok(cardService.buscarFatura(id, invoiceId, securityUtils.getUsuarioAutenticadoId()));
    }

    @PutMapping("/{id}/invoices/{invoiceId}")
    @Operation(summary = "Atualiza fatura")
    public ResponseEntity<CardInvoiceSummary> atualizarFatura(
            @PathVariable Long id,
            @PathVariable Long invoiceId,
            @RequestBody @Valid UpdateCardInvoiceRequest request
    ) {
        return ResponseEntity.ok(cardService.atualizarFatura(id, invoiceId, securityUtils.getUsuarioAutenticadoId(), request));
    }

    @PatchMapping("/{id}/invoices/{invoiceId}/fechar")
    @Operation(summary = "Fecha fatura")
    public ResponseEntity<CardInvoiceSummary> fecharFatura(@PathVariable Long id, @PathVariable Long invoiceId) {
        return ResponseEntity.ok(cardService.fecharFatura(id, invoiceId, securityUtils.getUsuarioAutenticadoId()));
    }

    @PatchMapping("/{id}/invoices/{invoiceId}/pagar")
    @Operation(summary = "Marca fatura como paga")
    public ResponseEntity<CardInvoiceSummary> pagarFatura(@PathVariable Long id, @PathVariable Long invoiceId) {
        return ResponseEntity.ok(cardService.pagarFatura(id, invoiceId, securityUtils.getUsuarioAutenticadoId()));
    }
}
