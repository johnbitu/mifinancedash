package dev.project.finance.dtos;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {}
