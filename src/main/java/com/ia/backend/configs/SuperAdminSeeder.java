package com.ia.backend.configs;

import com.ia.backend.entity.User;
import com.ia.backend.entity.UserRole;
import com.ia.backend.repository.UserRepository;
import com.ia.backend.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class SuperAdminSeeder {

    @Bean
    public ApplicationRunner SuperAdminInitializer(
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${application.super_admin.first_name}") String firstName,
            @Value("${application.super-admin.last-name}") String lastName,
            @Value("${application.super-admin.email}") String email,
            @Value("${application.super-admin.password}") String password
    ) {
        return args -> {
            UserRole superAdminRole = userRoleRepository.findByName("SUPER_ADMIN")
                    .orElseGet(() -> userRoleRepository.save(
                            UserRole.builder()
                                    .name("SUPER_ADMIN")
                                    .build()
                    ));

            if (!userRepository.existsByEmail(email)) {
                User superAdmin = User.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .roles(Set.of(superAdminRole))
                        .enabled(true)
                        .build();

                userRepository.save(superAdmin);
            }
        };
    }
}