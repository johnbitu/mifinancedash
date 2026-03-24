package dev.project.finance.services;

import dev.project.finance.dtos.LoginRequest;
import dev.project.finance.dtos.LoginResponse;
import dev.project.finance.dtos.RefreshRequest;
import dev.project.finance.dtos.RefreshResponse;
import dev.project.finance.dtos.UserLogin;
import dev.project.finance.exceptions.CredenciaisInvalidasException;
import dev.project.finance.models.RefreshToken;
import dev.project.finance.models.User;
import dev.project.finance.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final TokenBlacklistService tokenBlacklistService;
    private final long jwtExpirationInMillis;
    private final String dummyHash;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       AuditService auditService,
                       TokenBlacklistService tokenBlacklistService,
                       @Value("${jwt.expiration}") long jwtExpirationInMillis) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtExpirationInMillis = jwtExpirationInMillis;
        this.dummyHash = passwordEncoder.encode("dummy-password-fallback");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Optional<User> usuarioOpt = userRepository.findByEmail(request.email());
        String senhaHash = usuarioOpt.map(User::getSenha).orElse(dummyHash);

        boolean credenciaisValidas = passwordEncoder.matches(request.senha(), senhaHash)
                && usuarioOpt.isPresent();

        if (!credenciaisValidas) {
            auditService.registrar("LOGIN_FALHA", null, "N/A", false,
                    "Tentativa com email: " + request.email());
            throw new CredenciaisInvalidasException("Credenciais invalidas");
        }

        User user = usuarioOpt.get();
        auditService.registrar("LOGIN_SUCESSO", user.getId(), "N/A", true, null);

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.gerar(user);
        UserLogin login = new UserLogin(user.getId(), user.getEmail(), user.getRole());
        return new LoginResponse(accessToken, refreshToken.getToken(), jwtExpirationInMillis / 1000, login);
    }

    @Transactional
    public RefreshResponse renovar(RefreshRequest request) {
        RefreshToken refreshAtual = refreshTokenService.validar(request.refreshToken());
        refreshTokenService.revogar(refreshAtual);

        String novoAccessToken = jwtService.generateToken(refreshAtual.getUser());
        RefreshToken novoRefreshToken = refreshTokenService.gerar(refreshAtual.getUser());

        return new RefreshResponse(
                novoAccessToken,
                novoRefreshToken.getToken(),
                "Bearer",
                jwtExpirationInMillis / 1000
        );
    }

    @Transactional
    public void logout(String authorizationHeader, String refreshToken) {
        String token = extractBearerToken(authorizationHeader);
        if (token != null) {
            long ttl = Math.max(1,
                    jwtService.extractExpiration(token).toInstant().getEpochSecond() - Instant.now().getEpochSecond());
            tokenBlacklistService.revogar(token, ttl);
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revogarToken(refreshToken);
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7);
        return token.isBlank() ? null : token;
    }
}
