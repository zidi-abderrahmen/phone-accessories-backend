package com.ia.backend.password.repository;

import com.ia.backend.password.entity.ResetPassword;
import com.ia.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {

    Optional<ResetPassword> findByToken(String token);

    Optional<ResetPassword> findByUser(User user);
}