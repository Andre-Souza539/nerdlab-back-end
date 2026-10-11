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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class PostService {

    private final JpaPostRepository postRepository;

    public PostService(JpaPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        String slug;
        if (request.slug() != null && !request.slug().isBlank()) {
            slug = generateSlug(request.slug());
        } else {
            slug = generateSlug(request.title());
        }

        if (postRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException("Slug '" + slug + "' já está em uso.");
        }

        Instant publishedAt = null;
        if (request.status() == PostStatus.PUBLISHED) {
            publishedAt = Instant.now();
        }

        PostEntity post = PostEntity.builder()
                .title(request.title().trim())
                .slug(slug)
                .summary(request.summary().trim())
                .content(request.content())
                .status(request.status())
                .authorName(request.authorName().trim())
                .publishedAt(publishedAt)
                .build();

        PostEntity saved = postRepository.save(post);
        return toPostResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPosts(int page, int size, String sort, PostStatus status, String search) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = Math.min(50, Math.max(1, size));

        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize, sortObj);

        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<PostEntity> postPage = postRepository.findAllWithFilters(status, searchParam, pageable);

        return postPage.map(this::toPostSummaryResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post com ID " + id + " não encontrado."));
        return toPostResponse(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostBySlug(String slug) {
        PostEntity post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post com slug '" + slug + "' não encontrado."));
        return toPostResponse(post);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post com ID " + id + " não encontrado."));

        if (request.status() == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }

        post.setTitle(request.title().trim());
        post.setSummary(request.summary().trim());
        post.setContent(request.content());
        post.setStatus(request.status());

        PostEntity updated = postRepository.save(post);
        return toPostResponse(updated);
    }

    @Transactional
    public void deletePost(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post com ID " + id + " não encontrado."));
        postRepository.delete(post);
    }

    public static String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        String slug = withoutAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");
        return slug.replaceAll("^-|-$", "");
    }

    private static final java.util.Set<String> VALID_SORT_FIELDS = java.util.Set.of(
            "id", "title", "slug", "summary", "status", "authorName", "publishedAt", "createdAt", "updatedAt"
    );

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (!VALID_SORT_FIELDS.contains(property)) {
            throw new IllegalArgumentException("Campo de ordenação inválido: " + property);
        }
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }
        return Sort.by(direction, property);
    }

    private PostResponse toPostResponse(PostEntity entity) {
        return new PostResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSlug(),
                entity.getSummary(),
                entity.getContent(),
                entity.getStatus(),
                entity.getAuthorName(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PostSummaryResponse toPostSummaryResponse(PostEntity entity) {
        return new PostSummaryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSlug(),
                entity.getSummary(),
                entity.getStatus(),
                entity.getAuthorName(),
                entity.getPublishedAt(),
                entity.getCreatedAt()
        );
    }
}
