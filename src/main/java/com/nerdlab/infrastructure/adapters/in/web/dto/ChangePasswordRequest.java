package com.nerdlab.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitação de alteração de senha")
public record ChangePasswordRequest(
    @Schema(description = "Nova senha segura", example = "NovaSenhaForte456!")
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
    String newPassword
) {}
