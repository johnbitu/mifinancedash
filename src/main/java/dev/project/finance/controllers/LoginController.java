package dev.project.finance.controllers;

import dev.project.finance.dtos.LoginRequest;
import dev.project.finance.dtos.LoginResponse;
import dev.project.finance.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {

        LoginResponse response = authService.login(request);

        // 200 OK é o padrão pro login (tu não "criou" usuário, só autenticou)
        return ResponseEntity.ok(response);
    }
}