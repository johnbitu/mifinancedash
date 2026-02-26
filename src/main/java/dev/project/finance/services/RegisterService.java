package dev.project.finance.services;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.project.finance.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import dev.project.finance.dtos.RegisterRequest;
import dev.project.finance.dtos.RegisterResponse;
import dev.project.finance.exceptions.EmailAlreadyInUseException;
import dev.project.finance.models.User;
// import dev.project.finance.configs.SecurityConfig;

// RegisterService.java
@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        validateEmailAvailability(request.email());

        User newUser = buildUser(request);
        User savedUser = userRepository.save(newUser);

        return toResponse(savedUser);
    }

    private void validateEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("Esse email já está registrado: " + email);
        }
    }

    private User buildUser(RegisterRequest request) {
        return User.builder().name(request.nome()).email(request.email())
                .password(passwordEncoder.encode(request.senha())).createdAt(LocalDateTime.now()).build();
    }

    private RegisterResponse toResponse(User user) {
        return new RegisterResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}