package com.estsoft.project3.service;

import com.estsoft.project3.domain.ChatMessage;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.dto.ChatMessageDto;
import com.estsoft.project3.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final Map<Integer, List<DeferredResult<List<ChatMessageDto>>>> clients = new HashMap<>();

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
        for (int i = 0; i < 3; i++) {
            clients.put(i, new ArrayList<>());
        }
    }

    public void sendMessage(User user, int roomId, String message) {
        ChatMessage chatMessage = new ChatMessage(user, roomId, message);
        chatMessageRepository.save(chatMessage);

        ChatMessageDto messageDto = new ChatMessageDto(
            user.getNickname(),
            message,
            chatMessage.getSentAt()
        );

        for (DeferredResult<List<ChatMessageDto>> client : clients.get(roomId)) {
            client.setResult(List.of(messageDto));
        }

        clients.get(roomId).clear();
    }

    public DeferredResult<List<ChatMessageDto>> receiveMessage(int roomId) {
        DeferredResult<List<ChatMessageDto>> result = new DeferredResult<>(10000L, List.of());
        clients.get(roomId).add(result);
        return result;
    }

    public List<ChatMessageDto> getAllMessages(int roomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
        List<ChatMessageDto> result = new ArrayList<>();

        for (ChatMessage m : chatMessages) {
            ChatMessageDto dto = new ChatMessageDto(
                m.getUser().getNickname(),
                m.getMessage(),
                m.getSentAt()
            );
            result.add(dto);
        }

        return result;
    }

    @Scheduled(fixedRate = 3600000)
    public void deleteOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(1);
        chatMessageRepository.deleteBySentAtBefore(cutoff);
    }
}

