package com.estsoft.project3.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private int roomId;
    private String message;
    private LocalDateTime sentAt;

    protected ChatMessage() {
    }

    public ChatMessage(User user, int roomId, String message) {
        this.user = user;
        this.roomId = roomId;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }
}
