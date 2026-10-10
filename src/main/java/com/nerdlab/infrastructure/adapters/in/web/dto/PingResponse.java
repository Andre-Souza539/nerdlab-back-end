package com.nerdlab.infrastructure.adapters.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Resposta do endpoint de diagnóstico /ping")
public record PingResponse(
    @Schema(description = "Mensagem de status do ping", example = "pong")
    String message,

    @Schema(description = "Timestamp do servidor")
    Instant serverTime,

    @Schema(description = "Dados do usuário autenticado que realizou a chamada")
    UserSummaryResponse authenticatedUser
) {}
