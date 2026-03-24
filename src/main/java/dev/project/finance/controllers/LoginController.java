package dev.project.finance.controllers;

import dev.project.finance.dtos.LoginRequest;
import dev.project.finance.dtos.LoginResponse;
import dev.project.finance.dtos.LogoutRequest;
import dev.project.finance.dtos.RefreshRequest;
import dev.project.finance.dtos.RefreshResponse;
import dev.project.finance.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica usuario")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renova access token")
    public ResponseEntity<RefreshResponse> renovar(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(authService.renovar(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoga access token e refresh token")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) LogoutRequest request
    ) {
        String refreshToken = request == null ? null : request.refreshToken();
        authService.logout(authorization, refreshToken);
        return ResponseEntity.noContent().build();
    }
}
