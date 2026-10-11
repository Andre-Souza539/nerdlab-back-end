package com.nerdlab.infrastructure.adapters.in.web.dto;

import com.nerdlab.domain.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de nova publicação")
public record PostCreateRequest(
    @Schema(description = "Título da publicação", example = "Clean Architecture no Spring")
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
    String title,

    @Schema(description = "Slug amigável para URL (opcional, gerado automaticamente se omitido)", example = "clean-architecture-spring")
    @Size(max = 255, message = "O slug deve ter no máximo 255 caracteres")
    String slug,

    @Schema(description = "Resumo ou lead da publicação", example = "Um resumo técnico sobre arquitetura limpa.")
    @NotBlank(message = "O resumo é obrigatório")
    @Size(max = 500, message = "O resumo deve ter no máximo 500 caracteres")
    String summary,

    @Schema(description = "Conteúdo completo da postagem (suporta Markdown)", example = "## Introdução\nNeste artigo abordaremos...")
    @NotBlank(message = "O conteúdo é obrigatório")
    String content,

    @Schema(description = "Status inicial da publicação", example = "PUBLISHED")
    @NotNull(message = "O status é obrigatório")
    PostStatus status,

    @Schema(description = "Nome do autor", example = "Admin NerdLab")
    @NotBlank(message = "O autor é obrigatório")
    String authorName
) {}
