package com.roadrescue.repository;

import com.roadrescue.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRequestIdOrderBySentAtAsc(Long requestId);
}
