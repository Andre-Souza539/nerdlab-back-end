package com.nerdlab.infrastructure.adapters.in.web;

import com.nerdlab.application.services.PostService;
import com.nerdlab.domain.PostStatus;
import com.nerdlab.infrastructure.adapters.in.web.dto.*;
import com.nerdlab.infrastructure.adapters.in.web.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Post Management", description = "Endpoints de gerenciamento do ciclo de vida de posts e artigos no NerdLab")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Operation(summary = "Criar novo post", description = "Cadastra uma nova publicação no blog com título, slug, resumo, conteúdo, status e autor.")
    @ApiResponse(responseCode = "201", description = "Post cadastrado com sucesso",
            headers = @Header(name = "Location", description = "URI do recurso criado"),
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Slug já existente",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostResponse created = postService.createPost(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Listar posts paginados", description = "Retorna lista de posts com suporte a paginação, filtros de status, termo de busca e ordenação.")
    @ApiResponse(responseCode = "200", description = "Lista paginada de publicações retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos de ordenação ou consulta",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Page<PostSummaryResponse>> getPosts(
            @Parameter(description = "Índice da página (base 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página (padrão 10, máximo 50)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo e direção de ordenação (ex: publishedAt,desc ou createdAt,desc)")
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @Parameter(description = "Filtro opcional por status (DRAFT, PUBLISHED, ARCHIVED)")
            @RequestParam(required = false) PostStatus status,
            @Parameter(description = "Termo para busca em título ou resumo")
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(postService.getPosts(page, size, sort, status, search));
    }

    @GetMapping("/{identifier}")
    @Operation(summary = "Consultar post por ID ou slug", description = "Retorna detalhes completos do post através do ID numérico ou slug amigável.")
    @ApiResponse(responseCode = "200", description = "Post encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<PostResponse> getPostByIdentifier(@PathVariable String identifier) {
        if (identifier.matches("^\\d+$")) {
            return ResponseEntity.ok(postService.getPostById(Long.parseLong(identifier)));
        }
        return ResponseEntity.ok(postService.getPostBySlug(identifier));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Consultar post por slug", description = "Retorna detalhes completos do post através do slug amigável para SEO.")
    @ApiResponse(responseCode = "200", description = "Post encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHOR')")
    @Operation(summary = "Atualizar post existente", description = "Atualiza título, resumo, conteúdo e status de uma publicação existente.")
    @ApiResponse(responseCode = "200", description = "Post atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Post não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir post", description = "Exclui um post da plataforma pelo seu identificador único.")
    @ApiResponse(responseCode = "204", description = "Post excluído com sucesso")
    @ApiResponse(responseCode = "404", description = "Post não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
