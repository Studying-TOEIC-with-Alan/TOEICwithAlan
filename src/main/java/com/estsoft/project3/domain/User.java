package com.estsoft.project3.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false, length = 300)
    private String email;

    @Column(length = 100)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ROLE_ADMIN','ROLE_USER') DEFAULT 'ROLE_USER'")
    private Role role;

    @Column(nullable = false, length = 1, columnDefinition = "VARCHAR(1) DEFAULT 'Y'")
    private String isActive;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long grade = 0L;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long score = 0L;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDate;

    @Builder
    public User(String provider, String email, String nickname, Role role, String isActive) {
        this.provider = provider;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.isActive = isActive;
        this.grade = 0L;
        this.score = 0L;
    }

}
