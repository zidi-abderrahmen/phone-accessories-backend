package com.ia.backend.repository;

import com.ia.backend.entity.EmailVerification;
import com.ia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByToken(String token);

    Optional<EmailVerification> findByUser(User user);
}