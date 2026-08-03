package com.ia.backend.util;

import com.ia.backend.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record UserPrincipal(
        String email,
        String password,
        boolean enabled,
        boolean accountNonExpired,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String name = role.getName();
                    if (!name.startsWith("ROLE_")) {
                        name = "ROLE_" + name;
                    }
                    return new SimpleGrantedAuthority(name);
                })
                .collect(Collectors.toList());

        return new UserPrincipal(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                !user.isDeleted(),
                authorities
        );
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    @NonNull
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}