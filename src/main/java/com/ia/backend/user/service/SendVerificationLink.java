package com.ia.backend.user.service;

import com.ia.backend.user.entity.User;
import com.ia.backend.user.repository.UserRepository;
import com.ia.backend.user.util.TokenHasherUtils;
import com.ia.backend.user.verification.entity.EmailVerification;
import com.ia.backend.user.verification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendVerificationLink {

    @Value("${application.frontend.url}")
    private String baseUrl;
    
    private final TokenHasherUtils tokenHasher;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public User sendVerificationLink(User user, String tokenPath, boolean isResetPassword) {
        String rawToken = UUID.randomUUID().toString();
        EmailVerification emailVerification = EmailVerification.builder()
                .token(tokenHasher.hash(rawToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        user.setEmailVerification(emailVerification);

        User savedUser = userRepository.save(user);

        String verificationLink = baseUrl + tokenPath + rawToken;
        emailService.sendEmail(
                savedUser.getEmail(),
                (savedUser.getFirstName() + " " + savedUser.getLastName()),
                verificationLink,
                isResetPassword);

        return savedUser;
    }
}