package com.estsoft.project3.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ChatMessageDto {

    private String nickname;
    private String message;
    private LocalDateTime sentAt;

    public ChatMessageDto(String nickname, String message, LocalDateTime sentAt) {
        this.nickname = nickname;
        this.message = message;
        this.sentAt = sentAt;
    }
}