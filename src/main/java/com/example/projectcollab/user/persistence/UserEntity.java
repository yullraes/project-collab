package com.example.projectcollab.user.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected UserEntity() {
    }

    private UserEntity(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    public static UserEntity create(final String name, final String email) {
        return new UserEntity(name, email);
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
