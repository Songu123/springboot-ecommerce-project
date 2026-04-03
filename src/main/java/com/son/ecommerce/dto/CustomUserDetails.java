package com.son.ecommerce.dto;

import com.son.ecommerce.entity.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomUserDetails implements UserDetails {

    private User user;

    // Constructor
    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user != null ? user.getId() : null;
    }

    public String getFullName() {
        return user != null ? user.getFullName() : "";
    }

    public String getEmail() {
        return user != null ? user.getEmail() : "";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null) {
            return new HashSet<>();
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add roles
        if (user.getRoles() != null) {
            user.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority(role.getName()))
            );

            // Add permissions from roles
            user.getRoles().forEach(role -> {
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission ->
                        authorities.add(new SimpleGrantedAuthority(permission.getName()))
                    );
                }
            });
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : "";
    }

    @Override
    public String getUsername() {
        return user != null ? user.getUsername() : "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user != null && user.isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user != null && user.isEnabled();
    }
}
