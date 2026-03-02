package dev.project.finance.services;

import dev.project.finance.dtos.LoginRequest;
import dev.project.finance.dtos.LoginResponse;
import dev.project.finance.dtos.UserSummary;
import dev.project.finance.models.User;
import dev.project.finance.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        // 1) Buscar usuário
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2) Validar senha (BCrypt)
        boolean ok = passwordEncoder.matches(request.senha(), user.getSenha());
        if (!ok) {
            throw new RuntimeException("Senha inválida");
        }

        // 3) Gerar token (JWT)
        String token = jwtService.generateToken(user);

        // 4) Montar summary (dados mínimos pro front)
        UserSummary summary = new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
        // 5) Retornar resposta do login
        return new LoginResponse(token, "Bearer", 3600L, summary);
    }
}
