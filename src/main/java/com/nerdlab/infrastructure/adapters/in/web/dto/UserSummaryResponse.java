package com.nerdlab.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resumo dos dados públicos do usuário")
public record UserSummaryResponse(
    @Schema(description = "ID único do usuário", example = "1")
    Long id,

    @Schema(description = "Nome completo", example = "João da Silva")
    String name,

    @Schema(description = "Username do usuário", example = "joaosilva")
    String username,

    @Schema(description = "E-mail cadastrado", example = "joao@exemplo.com")
    String email,

    @Schema(description = "Perfil de autorização", example = "ROLE_USER")
    String role,

    @Schema(description = "Indica se precisa alterar senha", example = "false")
    boolean passwordChangeRequired,

    @Schema(description = "Data e hora de criação")
    LocalDateTime createdAt
) {}
