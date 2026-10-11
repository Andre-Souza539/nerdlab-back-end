package com.nerdlab.infrastructure.adapters.in.web.dto;

import com.nerdlab.domain.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização de postagem existente")
public record PostUpdateRequest(
    @Schema(description = "Título da publicação", example = "Clean Architecture no Spring Boot 3")
    @NotBlank(message = "O título é obrigatório")
    @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres")
    String title,

    @Schema(description = "Resumo ou lead da publicação", example = "Novo resumo atualizado do artigo.")
    @NotBlank(message = "O resumo é obrigatório")
    @Size(max = 500, message = "O resumo deve ter no máximo 500 caracteres")
    String summary,

    @Schema(description = "Conteúdo atualizado em Markdown", example = "## Introdução Atualizada\nConteúdo revisado...")
    @NotBlank(message = "O conteúdo é obrigatório")
    String content,

    @Schema(description = "Novo status da publicação", example = "PUBLISHED")
    @NotNull(message = "O status é obrigatório")
    PostStatus status
) {}
