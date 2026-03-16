package dev.project.finance.dtos;

public record RefreshResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {}