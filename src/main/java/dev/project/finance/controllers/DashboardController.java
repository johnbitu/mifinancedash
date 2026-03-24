package dev.project.finance.controllers;

import dev.project.finance.configs.SecurityUtils;
import dev.project.finance.dtos.DashboardResponse;
import dev.project.finance.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    @GetMapping("/resumo")
    @Operation(summary = "Resumo financeiro consolidado")
    public ResponseEntity<DashboardResponse> resumo() {
        return ResponseEntity.ok(dashboardService.resumo(securityUtils.getUsuarioAutenticadoId()));
    }
}
