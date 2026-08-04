package com.ia.backend.repository;

import com.ia.backend.entity.ResetPassword;
import com.ia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {

    Optional<ResetPassword> findByToken(String token);

    Optional<ResetPassword> findByUser(User user);

    void deleteAllByUser(User user);
}