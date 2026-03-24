package dev.project.finance.controllers;

import dev.project.finance.configs.SecurityUtils;
import dev.project.finance.dtos.CreateGoalRequest;
import dev.project.finance.dtos.DepositarGoalRequest;
import dev.project.finance.dtos.GoalProgressoResponse;
import dev.project.finance.dtos.GoalSummary;
import dev.project.finance.models.GoalStatus;
import dev.project.finance.models.User;
import dev.project.finance.services.GoalService;
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
@RequestMapping("/goals")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Goals")
public class GoalController {

    private final GoalService goalService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Cria meta")
    public ResponseEntity<GoalSummary> criar(@RequestBody @Valid CreateGoalRequest request) {
        User user = securityUtils.getUsuarioAutenticado();
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Lista metas")
    public ResponseEntity<List<GoalSummary>> listar(@RequestParam(required = false) GoalStatus status) {
        return ResponseEntity.ok(goalService.listar(securityUtils.getUsuarioAutenticadoId(), status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca meta por id")
    public ResponseEntity<GoalSummary> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.buscar(id, securityUtils.getUsuarioAutenticadoId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza meta")
    public ResponseEntity<GoalSummary> atualizar(@PathVariable Long id, @RequestBody @Valid CreateGoalRequest request) {
        return ResponseEntity.ok(goalService.atualizar(id, securityUtils.getUsuarioAutenticadoId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove meta")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        goalService.deletar(id, securityUtils.getUsuarioAutenticadoId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/depositar")
    @Operation(summary = "Deposita valor na meta")
    public ResponseEntity<GoalSummary> depositar(@PathVariable Long id, @RequestBody @Valid DepositarGoalRequest request) {
        return ResponseEntity.ok(goalService.depositar(id, securityUtils.getUsuarioAutenticadoId(), request));
    }

    @GetMapping("/{id}/progresso")
    @Operation(summary = "Resumo de progresso da meta")
    public ResponseEntity<GoalProgressoResponse> progresso(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.progresso(id, securityUtils.getUsuarioAutenticadoId()));
    }
}
