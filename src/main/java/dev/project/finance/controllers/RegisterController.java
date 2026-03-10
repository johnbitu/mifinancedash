package dev.project.finance.controllers;

import dev.project.finance.dtos.RegisterRequest;
import dev.project.finance.dtos.RegisterResponse;
import dev.project.finance.services.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request){

        RegisterResponse response = registerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
