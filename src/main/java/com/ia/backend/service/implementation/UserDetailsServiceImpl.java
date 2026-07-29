package com.ia.backend.service.implementation;

import com.ia.backend.entity.User;
import com.ia.backend.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isEnabled())

                .authorities(
                        user.getRoles().stream()
                                .map(role -> "ROLE_" + role.getName())

                                .map(SimpleGrantedAuthority::new)

                                .collect(Collectors.toList())
                )
                .build();
    }
}