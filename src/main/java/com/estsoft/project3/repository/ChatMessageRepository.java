package com.estsoft.project3.repository;

import com.estsoft.project3.domain.ChatMessage;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomIdOrderBySentAtAsc(int roomId);

    @Transactional
    void deleteBySentAtBefore(LocalDateTime cutoff);
}
