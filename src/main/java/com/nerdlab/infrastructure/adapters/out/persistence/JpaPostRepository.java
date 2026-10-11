package com.nerdlab.infrastructure.adapters.out.persistence;

import com.nerdlab.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPostRepository extends JpaRepository<PostEntity, Long> {

    boolean existsBySlug(String slug);

    Optional<PostEntity> findBySlug(String slug);

    @Query("SELECT p FROM PostEntity p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PostEntity> findAllWithFilters(@Param("status") PostStatus status,
                                        @Param("search") String search,
                                        Pageable pageable);
}
