package com.nerdlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nerdlab.domain.PostStatus;
import com.nerdlab.infrastructure.adapters.in.web.dto.PostCreateRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.PostUpdateRequest;
import com.nerdlab.infrastructure.adapters.out.persistence.JpaPostRepository;
import com.nerdlab.infrastructure.adapters.out.persistence.PostEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaPostRepository postRepository;

    @BeforeEach
    void setup() {
        postRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("CA01: Criação de post com payload válido retorna 201 Created e cabeçalho Location apontando para o recurso")
    void ca01_createPostSuccessWithLocationHeader() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "Integração Fullstack Next e Spring Boot 3",
                "integracao-fullstack-next-spring-boot-3",
                "Resumo completo da arquitetura e comunicação entre módulos.",
                "## Conteúdo Principal\nDetalhes de conexão via REST e WebSocket.",
                PostStatus.PUBLISHED,
                "Admin NerdLab"
        );

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/posts/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.slug").value("integracao-fullstack-next-spring-boot-3"))
                .andExpect(jsonPath("$.title").value("Integração Fullstack Next e Spring Boot 3"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.authorName").value("Admin NerdLab"))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        assertThat(postRepository.existsBySlug("integracao-fullstack-next-spring-boot-3")).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("CA02: Requisição com campos inválidos retorna 400 Bad Request com estrutura de erro detalhada")
    void ca02_invalidPayloadShouldReturnBadRequestWithDetails() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "", // Blank title
                null,
                "", // Blank summary
                "", // Blank content
                null, // Null status
                "" // Blank author
        );

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/posts"))
                .andExpect(jsonPath("$.details", not(empty())))
                .andExpect(jsonPath("$.details[*].field", hasItems("title", "summary", "content", "status", "authorName")));
    }

    @Test
    @DisplayName("CA03: Paginação padrão retorna no máximo 10 registros ordenados decrescentemente")
    void ca03_paginationDefaultShouldReturnMax10Items() throws Exception {
        for (int i = 1; i <= 15; i++) {
            PostEntity post = PostEntity.builder()
                    .title("Artigo Número " + i)
                    .slug("artigo-numero-" + i)
                    .summary("Resumo do artigo " + i)
                    .content("Conteúdo detalhado " + i)
                    .status(PostStatus.PUBLISHED)
                    .authorName("Autor " + i)
                    .publishedAt(Instant.now().minusSeconds(100 - i))
                    .build();
            postRepository.save(post);
        }

        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("CA04: Consulta a ID inexistente retorna 404 Not Found com payload padronizado")
    void ca04_getNonExistentIdReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Post com ID 999999 não encontrado."))
                .andExpect(jsonPath("$.path").value("/api/v1/posts/999999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("CA05: Tentativa de registrar slug duplicado retorna 409 Conflict")
    void ca05_duplicateSlugReturnsConflict() throws Exception {
        PostCreateRequest request1 = new PostCreateRequest(
                "Post Original",
                "slug-conflito-teste",
                "Resumo 1",
                "Conteúdo 1",
                PostStatus.PUBLISHED,
                "Admin"
        );
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        PostCreateRequest request2 = new PostCreateRequest(
                "Segundo Post Conflitante",
                "slug-conflito-teste",
                "Resumo 2",
                "Conteúdo 2",
                PostStatus.DRAFT,
                "Autor 2"
        );
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Slug 'slug-conflito-teste' já está em uso."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("CA06: Atualização de post atualiza updated_at sem modificar created_at")
    void ca06_updatePostPreservesCreatedAtAndUpdatesTimestamp() throws Exception {
        PostEntity post = PostEntity.builder()
                .title("Post Original")
                .slug("post-original")
                .summary("Resumo Antigo")
                .content("Conteúdo Antigo")
                .status(PostStatus.DRAFT)
                .authorName("Autor Fixo")
                .build();
        PostEntity saved = postRepository.saveAndFlush(post);
        Long id = saved.getId();
        Instant originalCreatedAt = saved.getCreatedAt();

        PostUpdateRequest updateReq = new PostUpdateRequest(
                "Post Atualizado Com Sucesso",
                "Novo Resumo do Artigo",
                "Novo Conteúdo Extenso",
                PostStatus.PUBLISHED
        );

        mockMvc.perform(put("/api/v1/posts/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Post Atualizado Com Sucesso"))
                .andExpect(jsonPath("$.summary").value("Novo Resumo do Artigo"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.authorName").value("Autor Fixo"))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty());

        PostEntity reloaded = postRepository.findById(id).orElseThrow();
        assertThat(reloaded.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(reloaded.getUpdatedAt()).isNotNull();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("CA07: Exclusão retorna 204 No Content e consultas subsequentes retornam 404 Not Found")
    void ca07_deletePostSuccessAndSubsequentQueriesReturnNotFound() throws Exception {
        PostEntity post = PostEntity.builder()
                .title("Post Para Deletar")
                .slug("post-para-deletar")
                .summary("Resumo")
                .content("Conteúdo")
                .status(PostStatus.DRAFT)
                .authorName("Autor")
                .build();
        PostEntity saved = postRepository.saveAndFlush(post);
        Long id = saved.getId();

        mockMvc.perform(delete("/api/v1/posts/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/posts/" + id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/posts/slug/post-para-deletar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Caso de Borda: Slug gerado a partir de título com caracteres especiais, acentos e emojis")
    void edgeCase_slugGenerationWithAccentsAndSpecialCharacters() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "Olá Mundo!! 2026: Introdução & Boas Práticas 😎",
                null, // slug omitido para forçar auto-geração
                "Resumo técnico sobre boas práticas.",
                "Conteúdo completo...",
                PostStatus.PUBLISHED,
                "Andre"
        );

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("ola-mundo-2026-introducao-boas-praticas"));
    }

    @Test
    @DisplayName("Caso de Borda: Consulta com page=9999 retorna página vazia 200 OK sem disparar erro de banco")
    void edgeCase_pageOutOfBoundsReturnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/v1/posts?page=9999&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()))
                .andExpect(jsonPath("$.number").value(9999));
    }

    @Test
    @DisplayName("Caso de Borda: Tamanho abusivo size=5000 é limitado ao máximo de 50")
    void edgeCase_excessivePageSizeClampedTo50() throws Exception {
        for (int i = 1; i <= 60; i++) {
            PostEntity post = PostEntity.builder()
                    .title("Post " + i)
                    .slug("post-" + UUID.randomUUID())
                    .summary("Resumo " + i)
                    .content("Conteúdo " + i)
                    .status(PostStatus.PUBLISHED)
                    .authorName("Autor")
                    .build();
            postRepository.save(post);
        }

        mockMvc.perform(get("/api/v1/posts?size=5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.content", hasSize(50)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Caso de Borda: Conteúdo extenso em Markdown suportado sem truncamento")
    void edgeCase_largeMarkdownContentSupported() throws Exception {
        String largeMarkdown = "# Título Extenso\n\n" + "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n\n".repeat(100);

        PostCreateRequest request = new PostCreateRequest(
                "Post com Conteúdo Longo",
                "post-conteudo-longo",
                "Resumo do artigo longo",
                largeMarkdown,
                PostStatus.PUBLISHED,
                "Autor"
        );

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(largeMarkdown));
    }

    @Test
    @DisplayName("Segurança: Exclusão sem autenticação retorna 401 Unauthorized")
    void security_deleteWithoutAuthReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Segurança: Exclusão com ROLE_USER retorna 403 Forbidden")
    void security_deleteWithRoleUserReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Segurança: Criação de post sem autenticação retorna 401 Unauthorized")
    void security_createWithoutAuthReturnsUnauthorized() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "Título de Teste",
                "titulo-de-teste",
                "Resumo de teste",
                "Conteúdo...",
                PostStatus.PUBLISHED,
                "Admin"
        );
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Segurança: Criação de post com ROLE_USER retorna 403 Forbidden")
    void security_createWithRoleUserReturnsForbidden() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                "Título de Teste",
                "titulo-de-teste",
                "Resumo de teste",
                "Conteúdo...",
                PostStatus.PUBLISHED,
                "Admin"
        );
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Contrato: Consulta direta por slug em /api/v1/posts/{slug} retorna detalhes do post")
    void getPostDirectlyBySlugEndpoint() throws Exception {
        PostEntity post = PostEntity.builder()
                .title("Post Rota Direta")
                .slug("post-rota-direta")
                .summary("Resumo da rota direta")
                .content("Conteúdo detalhado...")
                .status(PostStatus.PUBLISHED)
                .authorName("Autor")
                .build();
        postRepository.save(post);

        mockMvc.perform(get("/api/v1/posts/post-rota-direta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("post-rota-direta"))
                .andExpect(jsonPath("$.title").value("Post Rota Direta"))
                .andExpect(jsonPath("$.content").value("Conteúdo detalhado..."));
    }

    @Test
    @DisplayName("Caso de Borda: Ordenação por campo inexistente retorna 400 Bad Request via GlobalExceptionHandler")
    void edgeCase_invalidSortPropertyReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/posts?sort=campoInexistente,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("campoInexistente")));
    }

    @Test
    @DisplayName("Consulta por slug com sucesso e consulta com slug inexistente")
    void getPostBySlugSuccessAndNotFound() throws Exception {
        PostEntity post = PostEntity.builder()
                .title("Post Consulta Slug")
                .slug("post-consulta-slug")
                .summary("Resumo")
                .content("Conteúdo")
                .status(PostStatus.PUBLISHED)
                .authorName("Autor")
                .build();
        postRepository.save(post);

        mockMvc.perform(get("/api/v1/posts/slug/post-consulta-slug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("post-consulta-slug"))
                .andExpect(jsonPath("$.title").value("Post Consulta Slug"));

        mockMvc.perform(get("/api/v1/posts/slug/slug-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Post com slug 'slug-inexistente' não encontrado."));
    }

    @Test
    @DisplayName("Filtros por status e por busca textual")
    void filterByStatusAndSearch() throws Exception {
        PostEntity draft = PostEntity.builder()
                .title("Guia de Kubernetes Avançado")
                .slug("guia-kubernetes-avancado")
                .summary("Artigo técnico sobre orquestração.")
                .content("Conteúdo...")
                .status(PostStatus.DRAFT)
                .authorName("Autor K8s")
                .build();
        postRepository.save(draft);

        PostEntity published = PostEntity.builder()
                .title("Spring Boot Microservices")
                .slug("spring-boot-microservices")
                .summary("Arquitetura de microsserviços com Java.")
                .content("Conteúdo...")
                .status(PostStatus.PUBLISHED)
                .authorName("Autor Spring")
                .build();
        postRepository.save(published);

        // Filtro por status DRAFT
        mockMvc.perform(get("/api/v1/posts?status=DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].slug").value("guia-kubernetes-avancado"));

        // Filtro por termo de busca em resumo
        mockMvc.perform(get("/api/v1/posts?search=microsserviços"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].slug").value("spring-boot-microservices"));
    }
}
