package dev.project.finance.dtos;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        UserSummary user
) {}