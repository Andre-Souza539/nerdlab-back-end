package com.nerdlab.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com token JWT")
public record AuthResponse(
    @Schema(description = "Token JWT assinado")
    String token,

    @Schema(description = "Tipo do token", example = "Bearer")
    String tokenType,

    @Schema(description = "Tempo de expiração em segundos", example = "86400")
    long expiresInSeconds,

    @Schema(description = "Nome de usuário", example = "joaosilva")
    String username,

    @Schema(description = "Necessidade de alterar senha", example = "false")
    boolean passwordChangeRequired,

    @Schema(description = "Dados detalhados do usuário autenticado")
    UserSummaryResponse user
) {
    public static AuthResponse of(String token, long expiresInSeconds, UserSummaryResponse user) {
        return new AuthResponse(
            token,
            "Bearer",
            expiresInSeconds,
            user.username() != null ? user.username() : user.email(),
            user.passwordChangeRequired(),
            user
        );
    }
}
