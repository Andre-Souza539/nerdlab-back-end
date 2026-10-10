package com.nerdlab.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de novo usuário")
public record RegisterRequest(
    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
    String name,

    @Schema(description = "Nome de usuário (opcional)", example = "joaosilva")
    @Size(max = 100, message = "O username deve ter no máximo 100 caracteres")
    String username,

    @Schema(description = "E-mail válido", example = "joao@exemplo.com")
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @Schema(description = "Senha de acesso", example = "SenhaForte123!")
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    String password
) {}
