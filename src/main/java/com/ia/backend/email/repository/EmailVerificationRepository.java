package com.ia.backend.email.repository;

import com.ia.backend.email.entity.EmailVerification;
import com.ia.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByToken(String token);
}