package dev.project.finance.services;

import dev.project.finance.exceptions.TokenInvalidoException;
import dev.project.finance.models.RefreshToken;
import dev.project.finance.models.User;
import dev.project.finance.repositories.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final long EXPIRACAO_DIAS = 7L;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken gerar(User user) {
        // Revoga todos os tokens anteriores do usuário (rotação de token)
        refreshTokenRepository.revogarTodosPorUsuario(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiraEm(LocalDateTime.now().plusDays(EXPIRACAO_DIAS))
                .revogado(false)
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validar(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token não encontrado"));

        if (refreshToken.getRevogado()) {
            throw new TokenInvalidoException("Refresh token revogado");
        }

        if (refreshToken.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException("Refresh token expirado");
        }

        return refreshToken;
    }
}