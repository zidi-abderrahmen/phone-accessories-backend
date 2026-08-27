package com.ia.backend.repository;

import com.ia.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
        SELECT u
        FROM User u
        WHERE (:blocked IS NULL OR u.blocked = :blocked)
          AND (:deleted IS NULL OR u.deleted = :deleted)
    """)
    Page<User> findAllByBlockedAndDeleted(
            @Param("blocked") Boolean blocked,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );
}