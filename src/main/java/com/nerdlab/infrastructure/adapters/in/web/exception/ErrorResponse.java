package com.nerdlab.infrastructure.adapters.in.web.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Estrutura padronizada de erro da API")
public record ErrorResponse(
    @Schema(description = "Timestamp do erro")
    Instant timestamp,

    @Schema(description = "Código de status HTTP", example = "400")
    int status,

    @Schema(description = "Nome do erro HTTP", example = "BAD_REQUEST")
    String error,

    @Schema(description = "Mensagem amigável de erro", example = "Dados de entrada inválidos")
    String message,

    @Schema(description = "Caminho da requisição", example = "/api/v1/auth/register")
    String path,

    @Schema(description = "Lista detalhada de erros de validação por campo")
    List<FieldErrorDetail> details
) {
    public record FieldErrorDetail(
        @Schema(description = "Nome do campo com erro", example = "email")
        String field,

        @Schema(description = "Descrição da validação que falhou", example = "Formato de e-mail inválido")
        String message
    ) {}
}
