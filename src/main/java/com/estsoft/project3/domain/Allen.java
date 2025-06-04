package com.estsoft.project3.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "allen")
public class Allen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String category;

    @Column
    private String inputText;

    @Column
    private String summary;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDate;

    public Allen(User user, String category, String inputText, String summary) {
        this.user = user;
        this.category = category;
        this.inputText = inputText;
        this.summary = summary;
        this.createdDate = LocalDateTime.now();
    }

}
