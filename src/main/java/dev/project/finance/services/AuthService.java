package dev.project.finance.services;

import dev.project.finance.dtos.*;
import dev.project.finance.exceptions.CredenciaisInvalidasException;
import dev.project.finance.models.RefreshToken;
import dev.project.finance.models.User;
import dev.project.finance.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> usuarioOpt = userRepository.findByEmail(request.email());

        boolean credenciaisValidas = usuarioOpt
                .map(user -> passwordEncoder.matches(request.senha(), user.getSenha()))
                .orElse(false);

        if (!credenciaisValidas) {
            // Registra tentativa falha — userId null pois pode ser email inexistente
            auditService.registrar("LOGIN_FALHA", null, "N/A", false,
                    "Tentativa com email: " + request.email());
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }

        User user = usuarioOpt.get();
        auditService.registrar("LOGIN_SUCESSO", user.getId(), "N/A", true, null);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.gerar(user);
        UserLogin login = new UserLogin(user.getId(), user.getEmail(), user.getRole());
        return new LoginResponse(accessToken, refreshToken.getToken(), 3600L, login);
    }

    public RefreshResponse renovar(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.validar(request.refreshToken());
        String novoAccessToken = jwtService.generateToken(refreshToken.getUser());
        return new RefreshResponse(novoAccessToken, "Bearer", 3600L);
    }
}
