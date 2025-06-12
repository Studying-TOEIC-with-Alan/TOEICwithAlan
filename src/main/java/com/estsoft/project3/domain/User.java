package com.estsoft.project3.domain;

import com.estsoft.project3.contact.Contact;
import com.estsoft.project3.review.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column
    private LocalDate terminationDate;

    @Builder
    public User(String provider, String email, String nickname, Role role, String isActive) {
        this.provider = provider;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.isActive = isActive;
        this.grade = 0L;
        this.score = 0L;
        this.createdDate = LocalDateTime.now();
        this.terminationDate = null;
    }

    public boolean isOwner(Review review) {
        return this.userId.equals(review.getUser().getUserId());
    }

    public boolean isOwner(Contact contact) {
        return this.userId.equals(contact.getUser().getUserId());
    }

    public boolean isAdmin() {
        return this.role == Role.ROLE_ADMIN;
    }
}
