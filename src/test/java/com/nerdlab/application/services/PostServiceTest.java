package com.nerdlab.application.services;

import com.nerdlab.domain.PostStatus;
import com.nerdlab.infrastructure.adapters.in.web.dto.PostCreateRequest;
import com.nerdlab.infrastructure.adapters.in.web.dto.PostResponse;
import com.nerdlab.infrastructure.adapters.in.web.dto.PostSummaryResponse;
import com.nerdlab.infrastructure.adapters.in.web.dto.PostUpdateRequest;
import com.nerdlab.infrastructure.adapters.in.web.exception.DuplicateSlugException;
import com.nerdlab.infrastructure.adapters.in.web.exception.ResourceNotFoundException;
import com.nerdlab.infrastructure.adapters.out.persistence.JpaPostRepository;
import com.nerdlab.infrastructure.adapters.out.persistence.PostEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private JpaPostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Deve criar post com sucesso fornecendo slug explícito e status PUBLISHED")
    void shouldCreatePostWithExplicitSlug() {
        PostCreateRequest request = new PostCreateRequest(
                "Clean Architecture no Spring",
                "clean-architecture-spring",
                "Resumo técnico sobre arquitetura limpa.",
                "# Conteúdo em Markdown...",
                PostStatus.PUBLISHED,
                "Admin NerdLab"
        );

        PostEntity savedEntity = PostEntity.builder()
                .id(1L)
                .title(request.title())
                .slug("clean-architecture-spring")
                .summary(request.summary())
                .content(request.content())
                .status(request.status())
                .authorName(request.authorName())
                .publishedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(postRepository.existsBySlug("clean-architecture-spring")).thenReturn(false);
        when(postRepository.save(any(PostEntity.class))).thenReturn(savedEntity);

        PostResponse response = postService.createPost(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.slug()).isEqualTo("clean-architecture-spring");
        assertThat(response.title()).isEqualTo("Clean Architecture no Spring");
        assertThat(response.authorName()).isEqualTo("Admin NerdLab");
        assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(response.publishedAt()).isNotNull();

        verify(postRepository, times(1)).existsBySlug("clean-architecture-spring");
        verify(postRepository, times(1)).save(any(PostEntity.class));
    }

    @Test
    @DisplayName("Deve auto-gerar slug a partir do título quando omitido (incluindo acentos e caracteres especiais)")
    void shouldAutoGenerateSlugWithSpecialCharacters() {
        PostCreateRequest request = new PostCreateRequest(
                "Olá Mundo!! Spring Boot 3 & Java 21 - Guia Prático",
                null,
                "Resumo completo da publicação.",
                "Conteúdo completo...",
                PostStatus.DRAFT,
                "Andre Souza"
        );

        String expectedSlug = "ola-mundo-spring-boot-3-java-21-guia-pratico";

        when(postRepository.existsBySlug(expectedSlug)).thenReturn(false);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> {
            PostEntity entity = inv.getArgument(0);
            return PostEntity.builder()
                    .id(2L)
                    .title(entity.getTitle())
                    .slug(entity.getSlug())
                    .summary(entity.getSummary())
                    .content(entity.getContent())
                    .status(entity.getStatus())
                    .authorName(entity.getAuthorName())
                    .publishedAt(entity.getPublishedAt())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        });

        PostResponse response = postService.createPost(request);

        assertThat(response).isNotNull();
        assertThat(response.slug()).isEqualTo(expectedSlug);
        assertThat(response.status()).isEqualTo(PostStatus.DRAFT);
        assertThat(response.publishedAt()).isNull();

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo(expectedSlug);
    }

    @Test
    @DisplayName("Deve lançar DuplicateSlugException se slug já existir")
    void shouldThrowDuplicateSlugException() {
        PostCreateRequest request = new PostCreateRequest(
                "Clean Architecture",
                "clean-architecture",
                "Resumo",
                "Conteúdo",
                PostStatus.PUBLISHED,
                "Autor"
        );

        when(postRepository.existsBySlug("clean-architecture")).thenReturn(true);

        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(DuplicateSlugException.class)
                .hasMessageContaining("Slug 'clean-architecture' já está em uso.");

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar posts paginados com clamping de tamanho máximo de página em 50")
    void shouldListPostsWithPaginationClamping() {
        PostEntity post = PostEntity.builder()
                .id(1L)
                .title("Post 1")
                .slug("post-1")
                .summary("Resumo 1")
                .status(PostStatus.PUBLISHED)
                .authorName("Admin")
                .createdAt(Instant.now())
                .build();

        Page<PostEntity> page = new PageImpl<>(List.of(post));
        when(postRepository.findAllWithFilters(eq(PostStatus.PUBLISHED), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Page<PostSummaryResponse> result = postService.getPosts(-1, 5000, "createdAt,desc", PostStatus.PUBLISHED, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Post 1");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findAllWithFilters(eq(PostStatus.PUBLISHED), isNull(), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
        assertThat(capturedPageable.getPageSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("Deve buscar post por ID quando existente")
    void shouldGetPostById() {
        PostEntity post = PostEntity.builder()
                .id(10L)
                .title("Título Teste")
                .slug("titulo-teste")
                .summary("Resumo")
                .content("Conteúdo")
                .status(PostStatus.PUBLISHED)
                .authorName("Autor")
                .build();

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        PostResponse response = postService.getPostById(10L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.slug()).isEqualTo("titulo-teste");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar post inexistente por ID")
    void shouldThrowWhenPostNotFoundById() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post com ID 999 não encontrado.");
    }

    @Test
    @DisplayName("Deve buscar post por slug com sucesso")
    void shouldGetPostBySlug() {
        PostEntity post = PostEntity.builder()
                .id(1L)
                .title("Post Slug")
                .slug("post-slug")
                .summary("Resumo")
                .content("Conteúdo")
                .status(PostStatus.PUBLISHED)
                .authorName("Autor")
                .build();

        when(postRepository.findBySlug("post-slug")).thenReturn(Optional.of(post));

        PostResponse response = postService.getPostBySlug("post-slug");

        assertThat(response).isNotNull();
        assertThat(response.slug()).isEqualTo("post-slug");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar slug inexistente")
    void shouldThrowWhenSlugNotFound() {
        when(postRepository.findBySlug("slug-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostBySlug("slug-inexistente"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post com slug 'slug-inexistente' não encontrado.");
    }

    @Test
    @DisplayName("Deve atualizar post e preencher published_at se status mudar para PUBLISHED")
    void shouldUpdatePostAndSetPublishedAt() {
        PostEntity existing = PostEntity.builder()
                .id(5L)
                .title("Antigo Título")
                .slug("antigo-slug")
                .summary("Antigo Resumo")
                .content("Antigo Conteúdo")
                .status(PostStatus.DRAFT)
                .authorName("Autor Fixo")
                .publishedAt(null)
                .createdAt(Instant.now().minusSeconds(3600))
                .build();

        PostUpdateRequest updateRequest = new PostUpdateRequest(
                "Novo Título Atualizado",
                "Novo Resumo Atualizado",
                "Novo Conteúdo Atualizado",
                PostStatus.PUBLISHED
        );

        when(postRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponse updated = postService.updatePost(5L, updateRequest);

        assertThat(updated).isNotNull();
        assertThat(updated.title()).isEqualTo("Novo Título Atualizado");
        assertThat(updated.summary()).isEqualTo("Novo Resumo Atualizado");
        assertThat(updated.status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(updated.publishedAt()).isNotNull();
        assertThat(updated.authorName()).isEqualTo("Autor Fixo");
        assertThat(updated.slug()).isEqualTo("antigo-slug");
    }

    @Test
    @DisplayName("Deve excluir post existente com sucesso")
    void shouldDeletePostSuccessfully() {
        PostEntity post = PostEntity.builder()
                .id(7L)
                .title("Post a excluir")
                .build();

        when(postRepository.findById(7L)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(post);

        postService.deletePost(7L);

        verify(postRepository, times(1)).delete(post);
    }
}
