package com.parwaan.portfolio.model;

import jakarta.persistence.*;
import com.parwaan.portfolio.dto.UserDto;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'USER'")
    @Builder.Default
    private Role role = Role.USER;

    private Instant createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, we return an empty list. Later, you can map roles here.
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        // Spring Security's "username" is our email
        return email;
    }

    // For simplicity, we'll return true for these.
    // You can add logic for account locking, expiration, etc. later.
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public UserDto toDto() {
        return UserDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .build();
    }
}
