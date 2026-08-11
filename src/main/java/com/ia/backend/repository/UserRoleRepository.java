package com.ia.backend.repository;

import com.ia.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByName(String name);

    List<UserRole> findAllByDeleted(boolean deleted);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);
}