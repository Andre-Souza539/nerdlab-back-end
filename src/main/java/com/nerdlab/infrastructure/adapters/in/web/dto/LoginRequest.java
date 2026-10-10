package com.nerdlab.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para login")
public record LoginRequest(
    @Schema(description = "E-mail ou nome de usuário", example = "joao@exemplo.com")
    String email,

    @Schema(description = "Username alternativo", example = "joaosilva")
    String username,

    @Schema(description = "Senha do usuário", example = "SenhaForte123!")
    @NotBlank(message = "A senha é obrigatória")
    String password
) {
    public String getPrincipal() {
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return "";
    }
}
