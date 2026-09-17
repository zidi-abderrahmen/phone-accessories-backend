package com.ia.backend.bootstrap;

import com.ia.backend.user.entity.User;
import com.ia.backend.user.entity.UserRole;
import com.ia.backend.user.repository.UserRepository;
import com.ia.backend.user.repository.UserRoleRepository;
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
            @Value("${application.super_admin.last_name}") String lastName,
            @Value("${application.super_admin.email}") String email,
            @Value("${application.super_admin.password}") String password
    ) {
        return args -> {
            findOrCreateRole(userRoleRepository, "USER");
            findOrCreateRole(userRoleRepository, "ADMIN");
            UserRole superAdminRole = findOrCreateRole(userRoleRepository, "SUPER_ADMIN");

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

    private static UserRole findOrCreateRole(UserRoleRepository userRoleRepository, String name) {
        return userRoleRepository.findByName(name)
                .orElseGet(() -> userRoleRepository.save(
                        UserRole.builder()
                                .name(name)
                                .build()
                ));
    }
}