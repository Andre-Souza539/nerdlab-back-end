package com.nerdlab.infrastructure.adapters.in.web.dto;

import com.nerdlab.domain.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resumo da publicação para listagens e paginação")
public record PostSummaryResponse(
    @Schema(description = "Identificador único do post", example = "1")
    Long id,

    @Schema(description = "Título da publicação", example = "Clean Architecture no Spring")
    String title,

    @Schema(description = "Slug amigável para URL", example = "clean-architecture-spring")
    String slug,

    @Schema(description = "Resumo da publicação", example = "Um resumo técnico sobre arquitetura limpa.")
    String summary,

    @Schema(description = "Status da publicação", example = "PUBLISHED")
    PostStatus status,

    @Schema(description = "Nome do autor", example = "Admin NerdLab")
    String authorName,

    @Schema(description = "Data e hora de publicação")
    Instant publishedAt,

    @Schema(description = "Data de criação")
    Instant createdAt
) {}
