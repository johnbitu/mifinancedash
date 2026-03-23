package dev.project.finance.services;

import dev.project.finance.dtos.RegisterRequest;
import dev.project.finance.dtos.RegisterResponse;
import dev.project.finance.exceptions.EmailAlreadyInUseException;
import dev.project.finance.models.Roles;
import dev.project.finance.models.User;
import dev.project.finance.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        validateEmailAvailability(request.email());

        User newUser = buildUser(request);
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyInUseException("Esse email ja esta registrado: " + request.email());
        }

        return toResponse(savedUser);
    }

    private void validateEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("Esse email ja esta registrado: " + email);
        }
    }

    private User buildUser(RegisterRequest request) {
        return User.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(Roles.USUARIO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private RegisterResponse toResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
